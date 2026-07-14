# Digitalni album

Android aplikacija za digitalni album sličica (fudbalski / World Cup stil). Korisnici skupljaju sličice iz paketića, pregledaju album, prate napredak, igraju Memory igru za poene i simuliraju trade za nedostajuće sličice.

**Package:** `com.nejracoric.digitalnialbum`  
**Verzija:** 1.0  
**Jezik UI:** bosanski (podržan i engleski u onboarding-u)

---

## Tehnologije

| Tehnologija | Upotreba |
|-------------|----------|
| Kotlin 2.2 | Cijela aplikacija |
| Jetpack Compose + Material 3 | UI |
| MVVM | Screen ↔ ViewModel ↔ Repository |
| Navigation Compose | Rute i navigacija |
| Room 2.7 | Lokalna baza kataloga i kolekcije |
| DataStore Preferences | Postavke, poeni, free paketići, Memory progres |
| Retrofit + OkHttp + Gson | API (katalog, paketići, grbovi) |
| Coil | Učitavanje i disk cache slika |
| KSP | Room compiler |
| SwipingCards | Animirani deck pri otvaranju paketića |
| Android SplashScreen API | Sistemski splash |

**SDK:** `minSdk 33` · `targetSdk / compileSdk 35`

---

## Arhitektura

```
ui/          → ekrani, komponente, tema, navigacija
data/
  local/     → Room (AppDatabase, Entity, Dao)
  remote/    → Retrofit API + ApiConfig
  repository → StickerRepository (jedini izvor istine za sličice)
  preferences→ UserPreferences + Economy
  model/     → domain modeli (Sticker, layout, filteri…)
util/        → ImageCache, ShareUtil, NetworkMonitor, raritet…
```

DI je ručni (`DigitalAlbumApp` drži `StickerRepository` i `UserPreferences`). Nema Hilt/Koin.

---

## Funkcionalnosti

### Splash i onboarding
- Branded splash (~2.2 s), zatim onboarding ili glavni ekran.
- Onboarding: izbor jezika (BS / EN), označava se kao završen u DataStore-u.

### Album
- Katalog svih sličica iz lokalne Room baze.
- Filteri: sve / skupljeno / duplikati (i podrška za nedostajuće).
- Pretraga, sortiranje, filter po timu.
- Progress (skupljeno / ukupno).
- Pull-to-refresh sinhronizacija s API-jem (pažljivo zbog limita).
- Tablet: šira mreža (npr. 3 kolone).
- Tap → detalj sličice.

### Paketići
- Otvaranje paketića od **5** unique sličica s API-ja.
- Efekat folije / otkrivanja + SwipingCards deck.
- **“Otvori novi”** troši API poziv; inače se može ponovo pregledati zadnji paketić iz sesije (Room / lokalno).
- Ekonomija:
  - **2 besplatna** paketića
  - nakon toga **5 poena** po paketiću
- Shake-to-open podrška (osjetljiviji gest).

### Detalj sličice
- Prikaz sličice, tima, rijetkosti.
- Akcije u top baru: **Sačuvano** (bookmark / wishlist), Podijeli, Favorit.
- Rijetkost na osnovu `tip_slicice` / `vjerovatnoca` (i stabilan roll za katalog).

### Sačuvane sličice (wishlist)
- Ulaz: **Profil → Sačuvane sličice**.
- Lista svih sličica označenih bookmarkom na detalju.
- Čuva se u Room tabeli `wishlist`.
- Prikazuje status “Već imaš” / “Tražiš”; tap otvara detalj.

### Statistika
- Pregled kolekcije / napretka.
- Link ka listi duplikata.

### Favoriti
- Lista omiljenih sličica (Room tabela `favorites`).

### Profil (Postavke)
- Prikaz **poena**.
- Ulazi u **Memory**, **Trade** i **Sačuvane sličice**.
- Dijeljenje listi (system share intent):
  - lista nedostajućih
  - lista duplikata
- Prikaz albuma: Lista / Mreža.
- Omiljeni tim.

### Duplikati
- Sličice s `ownedCount > 1`.
- Share liste.

### Share
- Pojedinačna sličica, nedostajuće ili duplikati preko `ShareUtil` (text intent).

### Memory igra
- Ulaz: Profil → Memory.
- Memory match s kartama iz **keširanih sličica i grbova** (lokalni fajlovi).
- Leveli 1–8, sve teži (više parova, više vremena, različiti layouti kolona).
- Bodovi za pobjedu: `max(preostalo_vrijeme / 3, 1)`.
- Sljedeći level se otključava nakon pobjede.
- Poeni se troše na paketiće.

| Level | Parova | Vrijeme (s) | Kolone |
|------:|-------:|------------:|-------:|
| 1 | 4 | 45 | 4 |
| 2 | 6 | 50 | 4 |
| 3 | 8 | 55 | 4 |
| 4 | 10 | 60 | 5 |
| 5 | 12 | 70 | 4 |
| 6 | 14 | 80 | 4 |
| 7 | 16 | 90 | 4 |
| 8 | 18 | 100 | 6 |

### Trade (simulacija)
- Ulaz: Profil → Trade.
- Lista **nedostajućih** sličica.
- Tap → generisani “korisnici” koji navodno imaju tu sličicu.
- Razmjena: daš **duplikat** → dobiješ nedostajuću + poene:
  - fair trade → **+1.0** poen
  - unfair → **+0.5** poena
- Nema pravog multiplayer API-ja — korisnici su lokalno generisani.

---

## Ekonomija poena

| Akcija | Efekat |
|--------|--------|
| Pobjeda u Memory | +poeni (vrijeme / 3, min 1) |
| Trade | +1.0 ili +0.5 |
| Besplatni paketići | prva 2 |
| Plaćeni paketić | −5 poena |

Poeni se čuvaju u DataStore (`album_points`).

---

## Podaci

### Room (`digitalni_album.db`, version 2)

| Tabela | Sadržaj |
|--------|---------|
| `stickers` | Katalog sličica |
| `owned_stickers` | Primjerci koje korisnik ima (više redova = duplikati) |
| `favorites` | Favoriti |
| `wishlist` | Sačuvano / wishlist |

### DataStore (`user_prefs`)
- Onboarding završen, jezik, omiljeni tim, layout liste
- Poeni, broj iskorištenih free paketića, otključani Memory level

### API
**Base URL:** `http://49.13.125.189:3300/`

Primjeri endpointa:
- `GET api/all-players` — katalog
- `GET api/random-players-unique/{count}` — paketić
- `GET api/grbovi` — grbovi
- Slike: `/api/slicica/{id}`, `/api/grb/{teamCode}`

**Važno:** API ima limit **100 poziva / sat**. App:
- kešira katalog u Room
- ne zove “sve” pri svakom otvaranju
- pri 429 prikazuje poruku o limitu
- force refresh samo na pull-to-refresh / eksplicitni sync

Cleartext HTTP za ovaj host je dozvoljen preko network security config.

### Image cache
- Lokalni folderi: `filesDir/cache/stickers/`, `cache/crests/`
- Prefetch pri sync-u / paketiću (semafor 3 paralelna downloada)
- Memory igra koristi samo keširane fajlove
- Coil: memorijski + disk cache

---

## UI / dizajn

- Dark glassmorphic stil (navy pozadina, glass kartice)
- Akcenti: gold + neon cyan (+ magenta holografski efekti na paketićima)
- Shared komponente: `GlassBackground`, `GlassCard`, `PlayerGlassCard`, `GlassChip`, `AppTopBar`
- Custom app ikona + splash logo
- Bottom navigation (Album, Paketići, Statistika, Favoriti, Profil)

---

## Navigacija (glavne rute)

| Ruta | Ekran |
|------|--------|
| `splash` | Splash |
| `onboarding` | Onboarding |
| `main` | Tabovi |
| `detail/{stickerId}` | Detalj |
| `favorites` | Favoriti |
| `settings` | Profil / postavke |
| `wishlist` | Sačuvane sličice |
| `duplicates` | Duplikati |
| `memory` | Memory igra |
| `trade` | Trade |

---

## Pokretanje

Zahtjevi: Android Studio (Ladybug+ / agp 8.13), JDK 11+, uređaj/emulator **API 33+**, mreža za prvi sync i paketiće.

```bash
./gradlew assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

Ili Run ▶ iz Android Studija.

---

## Struktura projekta (sažeto)

```
app/src/main/java/com/nejracoric/digitalnialbum/
├── DigitalAlbumApp.kt
├── MainActivity.kt
├── data/
│   ├── local/          # Room
│   ├── remote/         # Retrofit
│   ├── repository/
│   ├── preferences/    # DataStore + Economy
│   └── model/
├── ui/
│   ├── album/
│   ├── pack/
│   ├── detail/
│   ├── favorites/
│   ├── stats/
│   ├── settings/
│   ├── duplicates/
│   ├── memory/
│   ├── trade/
│   ├── splash/
│   ├── onboarding/
│   ├── components/
│   ├── theme/
│   └── navigation/
└── util/               # ImageCache, ShareUtil, …
```

---

## Šta je urađeno (pregled isporuke)

- [x] Digitalni album sličica s Room katologom i offline pregledom
- [x] Sync s REST API-jem + poštovanje rate limita
- [x] Otvaranje paketića (5 sličica), folija, SwipingCards
- [x] Free / plaćeni paketići poenima
- [x] Favoriti, sačuvane (wishlist), duplikati, statistika
- [x] Detalj sličice + share
- [x] Glassmorphic UI, custom ikona i splash
- [x] Image disk cache (sličice + grbovi)
- [x] Memory igra po levelima + poeni
- [x] Simulirani Trade (duplikat ↔ nedostajuća + poeni)
- [x] Profil: poeni, layout, omiljeni tim, Sačuvane, ulazi u igrice

---

## Ograničenja / napomene

- Trade **nije** pravi multiplayer — nema backend trade API-ja.
- Bez mreže nema novih paketića / force refresh-a; lokalni album, Sačuvane i Memory (ako ima keša) rade.
- API je HTTP (cleartext) na fiksnoj IP adresi.

---

## Licenca

Studentski / projektni rad — Digitalni album.
