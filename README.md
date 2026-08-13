# Kalender Hijriah

Aplikasi kalender Hijriah untuk Android dengan tanggal Hijriah sebagai pengalaman utama, jadwal salat, alarm adzan, arah Kiblat, tiga tema, dan ikon aplikasi yang berubah mengikuti tanggal.

> Versi terbaru: **1.10.7**
> Android minimum: **Android 8.0 (API 26)**  
> Paket aplikasi: `id.kalender.hijriah`

## Fitur

- Kalender Hijriah atau Masehi sebagai kalender utama.
- Pilihan awal pekan Jumat atau Minggu.
- Tanggal Hijriah berganti saat Maghrib; tanggal Masehi berganti pukul 00.00.
- Ikon dinamis:
  - mode Hijriah menampilkan tanggal 1–30 dan fase bulan Hijriah;
  - mode Masehi menampilkan angka tanggal Masehi;
  - warna latar ikon mengikuti tema aplikasi.
- Widget ikon dinamis 1×1 sebagai solusi untuk launcher yang menyimpan cache ikon layar utama.
- Widget jadwal salat 4×1 satu baris yang mengikuti pilihan urutan Subuh atau Maghrib.
- Tiga tema: Malam Zamrud, Fajar Safir, dan Zaitun Pasir.
- Jadwal salat berdasarkan lokasi dengan pilihan metode perhitungan.
- Pilihan awal urutan jadwal salat: Subuh atau Maghrib.
- Koreksi waktu setiap salat dari −5 sampai +5 menit.
- Alarm salat dengan adzan bawaan, nada bawaan ponsel, atau audio lokal pilihan pengguna.
- Pratinjau otomatis ketika suara adzan dipilih.
- Volume adzan khusus aplikasi 0–100% yang tidak mengubah volume alarm atau media ponsel.
- Tombol **Hentikan adzan** pada notifikasi alarm.
- Arah Kiblat berbasis Android Rotation Vector, koreksi utara sebenarnya, panduan kalibrasi, serta petunjuk putar kanan/kiri yang mudah dipahami.
- Koreksi tanggal Hijriah untuk menyesuaikan keputusan rukyat/hisab setempat.

## Unduh APK

Unduh APK terbaru dari bagian **Releases** pada halaman GitHub proyek ini, lalu pilih berkas:

`Kalender-Hijriah-v1.10.7-Qibla-Redesign.apk`

SHA-256 APK versi 1.10.7:

```text
4E091D223AE9402C4DFAADDCB39402785C6E532F727041CAD11ACC5B0550D064
```

Pastikan nama berkas dan checksum sesuai sebelum memasang. APK resmi ditandatangani; pembaruan berikutnya harus memakai tanda tangan yang sama agar dapat dipasang di atas versi lama.

## Cara memasang di Android

Nama menu bisa sedikit berbeda antar merek ponsel. Langkah berikut sesuai dengan Android 12 dan tetap relevan untuk versi Android lain.

1. Buka halaman **Releases**, lalu unduh APK dari bagian **Assets**.
2. Jika Chrome menampilkan peringatan bahwa berkas APK mungkin berbahaya, pastikan alamatnya benar-benar berasal dari halaman rilis repositori ini, lalu pilih **Download anyway / Tetap download / Proceed anyway**.
3. Buka APK dari notifikasi unduhan atau aplikasi **File/Downloads**.
4. Jika muncul pesan bahwa pemasangan dari sumber ini belum diizinkan, pilih **Settings / Setelan**.
5. Aktifkan **Allow from this source / Izinkan dari sumber ini** hanya untuk aplikasi yang dipakai mengunduh atau membuka APK, lalu kembali.
6. Pilih **Install / Pasang**.

### Jika Google Play Protect meminta pemindaian

1. Pilih **Scan app / Pindai aplikasi**.
2. Tunggu sampai pemindaian selesai.
3. Jika hasilnya aman, lanjutkan dengan **Install / Pasang**.
4. Pada beberapa perangkat, opsi lanjut berada di **More details / Detail selengkapnya**, kemudian **Install anyway / Tetap pasang / Proceed anyway**.

Gunakan **Install anyway** hanya apabila APK berasal dari rilis resmi proyek ini dan checksum SHA-256-nya cocok. Jangan lanjutkan bila Play Protect menyatakan aplikasi berbahaya atau checksum berbeda.

Setelah instalasi selesai, Anda boleh menonaktifkan kembali **Allow from this source** untuk keamanan.

## Ikon dinamis di layar utama

Sebagian launcher, termasuk launcher tertentu pada Android 12, menyimpan gambar ikon aplikasi yang sudah ditempel di layar utama. Aplikasi tidak dapat memaksa launcher menghapus cache tersebut. Ikon di daftar aplikasi dapat sudah berubah sementara ikon lama di layar utama belum ikut berubah.

Gunakan widget yang disediakan aplikasi agar tanggal di layar utama diperbarui secara konsisten:

1. Tekan lama area kosong di layar utama.
2. Pilih **Widget**.
3. Cari **Kalender Hijriah Dinamis**.
4. Tarik widget ukuran 1×1 ke layar utama.

Widget mengikuti kalender utama dan tema yang dipilih. Mode Hijriah berganti saat Maghrib; mode Masehi berganti pukul 00.00. Widget dapat diketuk untuk membuka aplikasi.

### Widget jadwal salat

Untuk menampilkan lima waktu salat dalam satu baris:

1. Tekan lama area kosong di layar utama.
2. Pilih **Widget**.
3. Cari **Jadwal Salat**.
4. Tarik widget ukuran 4×1 ke layar utama.

Urutan pada widget otomatis mengikuti pengaturan **Awal urutan jadwal salat**: Subuh atau Maghrib. Waktu, metode perhitungan, koreksi menit, lokasi, dan warna tema juga mengikuti pengaturan aplikasi.

## Izin yang digunakan

- **Lokasi presisi/perkiraan** — menghitung jadwal salat, waktu Maghrib, dan arah Kiblat.
- **Notifikasi, alarm tepat waktu, getar, dan wake lock** — menjalankan alarm salat pada waktunya.
- **Mulai setelah perangkat menyala** — menjadwalkan ulang alarm dan memperbarui ikon setelah ponsel dinyalakan.

Pemilihan audio lokal memakai pemilih dokumen Android. Aplikasi tidak mengunggah file audio atau lokasi pengguna ke server aplikasi.

Pengaturan volume adzan merupakan pengali volume internal aplikasi. Pengaturan ini tidak menggeser volume alarm atau media sistem, tetapi keluaran akhir tetap mengikuti kemampuan speaker, perangkat audio yang tersambung, serta pembatasan mode senyap dari Android/perangkat.

## Catatan akurasi

- Kalender menggunakan `java.time.chrono.HijrahDate` (kalender Umm al-Qura yang tersedia di Android/Java).
- Awal bulan Hijriah resmi dapat berbeda 1–2 hari karena rukyat, hisab, dan keputusan otoritas setempat. Gunakan **Koreksi tanggal Hijriah** bila diperlukan.
- Jadwal salat adalah hasil perhitungan. Pilih metode yang sesuai dan gunakan koreksi menit untuk menyesuaikan jadwal resmi daerah.
- Arah Kiblat memakai Android Rotation Vector, dengan Geomagnetic Rotation Vector dan accelerometer + magnetometer sebagai fallback. Koreksi deklinasi magnetik diterapkan berdasarkan lokasi agar arah mengacu ke utara sebenarnya.
- Kompas ponsel tetap dapat dipengaruhi casing magnetik, logam, dan perangkat elektronik. Aktifkan lokasi, gunakan di tempat terbuka, lalu lakukan gerakan angka delapan saat diminta.

## Membangun dari source code

Kebutuhan:

- JDK 17
- Android SDK Platform 35
- Internet pada build pertama untuk mengunduh dependensi

Di Windows:

```powershell
./gradlew.bat :app:assembleDebug
```

Di Linux atau macOS:

```bash
./gradlew :app:assembleDebug
```

Hasil debug berada di `app/build/outputs/apk/debug/app-debug.apk`.

## Kredit dan sumber terbuka

Proyek ini berawal dari contoh kalender gabungan Masehi–Hijriah yang dipublikasikan oleh **ChTpx**:

- [ChTpx — Composite Gregorian and Islamic/Hijrah calendar (GitHub Gist)](https://gist.github.com/ChTpx/508839159bc1f8ba397ef0f720f42b69)

Contoh tersebut menjadi referensi awal untuk struktur kalender dua tanggal dan konversi Hijriah. Setelah itu, tampilan, navigasi, pergantian hari saat Maghrib, jadwal dan alarm salat, Kiblat, pengaturan, sistem tiga tema, serta ikon launcher dinamis dikembangkan khusus untuk aplikasi ini.

Dependensi dan media pihak ketiga:

- [Batoul Apps Adhan Java](https://github.com/batoulapps/adhan-java) — perhitungan waktu salat.
- **Beautiful Adhan**, oleh Adam-synagda, dari [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:Beautiful_adhan.ogg) — CC0 1.0 Universal.
- **Adhan Doha** dari [Internet Archive](https://archive.org/details/adhan.recordings.from.doha.qatar), berkas `Adhan_Doha_Qatar_02_Dhuhr_Adhan.ogg` — Public Domain Mark 1.0.

Rincian audio juga tersimpan di `app/src/main/assets/audio_licenses.txt`.

## Status lisensi proyek

Belum ada lisensi umum yang diberikan untuk source code khusus proyek ini. Kode dapat dilihat di GitHub, tetapi penggunaan ulang atau distribusi turunannya memerlukan izin pemilik proyek. Komponen pihak ketiga tetap mengikuti lisensi sumber masing-masing.
