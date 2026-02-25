# 📱 WhereWasMyTime — Zaman Takip Uygulaması

## 💡 Temel Fikir
Gün içinde hangi aktiviteye ne kadar vakit harcandığını takip eden, görsel raporlar sunan bir mobil uygulama.

---

## 🎯 Temel Özellikler (MVP)

### 1. Aktivite Takibi
- Uygulama açılınca kullanıcı bir kategori seçer (örn: Ev İşi, Ders, Programlama, Oyun, Spor, Sosyal Medya, Uyku vb.)
- "Başlat" tuşuna basınca kronometre çalışır
- "Durdur" ile oturum kaydedilir
- Birden fazla oturum aynı kategoriye eklenebilir

### 2. Günlük / Haftalık Özet
- O gün toplam ne kadar çalıştın, ne kadar eğlendin?
- Pasta grafik veya çubuk grafik ile görsel breakdown
- Haftanın en verimli günü / en az verimli günü karşılaştırması

### 3. Kategoriler
- Varsayılan kategoriler (özelleştirilebilir)
- Her kategoriye renk atanabilir
- İkon **veya özel fotoğraf** atanabilir (galeriden seç / kamerayla çek)
- Kullanıcı kendi kategorisini ekleyebilir / silebilir

---

## 🚀 Gelişmiş Özellikler

### 4. Fotoğraf ile Görsel İlerleme Kaydı
- Kullanıcı isteğe bağlı olarak oturum başında/sonunda fotoğraf çekebilir
- Günün fotoğrafları bir "günlük" gibi görüntülenebilir (kolaj veya timeline)
- "Bugün ne yaptım?" sorusuna görsel cevap

### 5. Konum Bazlı Otomatik Kategori Önerisi *(İsteğe Bağlı)*
- 15 dakikada bir arka planda konum kontrolü
- Kullanıcı `Ev`, `Okul`, `Spor Salonu` gibi yerleri kaydeder
- Uygun konuma gelince uygulama "Spor mu yapıyorsun?" diye önerir
- Kullanıcı onaylarsa otomatik takip başlar

### 6. Akıllı Bildirimler
- Günlük hedef: "Bugün 2 saat ders hedefin var, şimdiye kadar 45 dk yaptın"
- Haftalık özet bildirimi (Pazar akşamı)

### 7. Hedef Belirleme
- Kategorilere günlük/haftalık hedef saat atanabilir
- İlerleme çubuğu ile motivasyon sağlanır (örn: Programlama: 1.5h / 3h hedef)
- Hedef tutturulunca küçük kutlama animasyonu 🎉

### 8. İstatistik & Analitik Ekranı
- Son 7 gün, 30 gün, tüm zamanlar görünümü
- En çok vakit harcanan kategori sıralaması
- En verimli saat dilimleri (sabah mı akşam mı daha çok çalışıyorsun?)
- Streak (ardışık gün) takibi

### 9. Dışa Aktarma
- Günlük/aylık raporu PDF olarak dışa aktar
- CSV export (Excel ile analiz için)

---

## 🏗️ Teknik Mimari Önerisi

| Katman          | Seçenek                                         |
| --------------- | ----------------------------------------------- |
| **Platform**    | Android (Kotlin) veya Flutter (çapraz platform) |
| **Veritabanı**  | Room DB (yerel) veya SQLite                     |
| **Grafik**      | MPAndroidChart veya fl_chart (Flutter)          |
| **Tema**        | Material Design 3                               |
| **Konum**       | Fused Location Provider                         |
| **Bildirimler** | WorkManager + NotificationManager               |

---

## 🗂️ Veri Modeli (Temel)

```
Session {
  id: UUID
  categoryId: UUID
  startTime: DateTime
  endTime: DateTime
  durationMinutes: Int
  photoPath: String? (opsiyonel)
  note: String? (opsiyonel)
  location: LatLng? (opsiyonel)
}

Category {
  id: UUID
  name: String
  color: Int (hex)
  icon: String?          // yerleşik ikon (opsiyonel)
  photoPath: String?     // kullanıcının seçtiği özel fotoğraf (opsiyonel)
  dailyGoalMinutes: Int?
}
```

---

## 📐 Ekranlar (UI Akışı)

```
Anasayfa
  ├── Aktif Oturum Ekranı (kronometre)
  ├── Bugünün Özeti (pie chart)
  └── Son Aktiviteler listesi

İstatistikler
  ├── Günlük / Haftalık / Aylık görünüm
  └── Kategori bazlı detay

Kategoriler
  ├── Kategori listesi
  └── Kategori ekle/düzenle

Hedefler
  └── Kategori hedef ayarları

Ayarlar
  ├── Bildirim tercihleri
  ├── Konum izni yönetimi
  └── Tema (açık/koyu)
```

---

## 🛣️ Geliştirme Yol Haritası

### Aşama 1 — MVP (2-3 Hafta)
- [ ] Temel aktivite başlat/durdur akışı
- [ ] Kategori yönetimi
- [ ] Günlük özet ekranı (basit liste)
- [ ] Room DB entegrasyonu

### Aşama 2 — Görsellik (1-2 Hafta)
- [ ] Pasta grafiği ve haftalık çubuk grafik
- [ ] Tema sistemi (açık/koyu mod)

### Aşama 3 — Gelişmiş Özellikler (2-3 Hafta)
- [ ] Hedef belirleme sistemi
- [ ] Akıllı bildirimler (WorkManager)
- [ ] Fotoğraf kaydetme

### Aşama 4 — Bonus (İsteğe Bağlı)
- [ ] Konum bazlı kategori önerisi
- [ ] PDF/CSV export
- [ ] Widget desteği (ana ekran)

---

## 💭 Neden Bu Proje İyi Bir Seçim?

- **Öğretici**: Room DB, LiveData/StateFlow, WorkManager, izin yönetimi (konum, kamera) gibi Android geliştirme temel konularını kapsar
- **Portfolio için güçlü**: Gerçek hayatta kullanılan bir uygulama
- **Kendi ihtiyacına yönelik**: En iyi projeler kendi problemini çözenlerdir
- **Genişletilebilir**: MVP ile başlayıp katman katman büyütülebilir
- **Play Store'a çıkarılabilir**: Tamamlandığında yayınlanabilir seviyede