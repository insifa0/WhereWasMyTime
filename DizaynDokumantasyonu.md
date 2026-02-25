# 🎨 WhereWasMyTime — Sistem & Dizayn Dokümantasyonu

Bu doküman projenin *nasıl* yapılacağını, arka planda hangi mimari kararların alınacağını ve arayüz/deneyim (UI/UX) elementlerinin nasıl kurgulanacağını detaylandırır. 

---

## 1. Mimari ve Sistem Yaklaşımı (Architecture)

### 1.1 Teknoloji Yığını (Tech Stack)
- **Platform:** Sadece **Android** (Kotlin + Jetpack Compose ile modern native geliştirme).
- **Veritabanı (Faz 1 - Yerel):** Room Database ile veriler cihazda çevrimdışı olarak tutulacak.
- **Veritabanı & Kimlik Doğrulama (Faz 2 - Bulut):** Google Sign-In entegrasyonu ve verilerin ücretsiz olarak **Firebase (Firestore)** üzerinde yedeklenmesi/senkronize edilmesi.

### 1.2 Veri Saklama ve "Soft-Delete" (Silme Mantığı)
Kullanıcı bir kategoriyi sildiğinde geçmiş verilerin kaybolmaması için **Soft-Delete (Arşivleme)** mantığı uygulanacak:
- Tablolarda `isArchived` (veya `isDeleted`) adında bir boolean bayrak (flag) olacak.
- Kategori silinirse sadece bu bayrak `true` yapılacak, böylece Kategori Listesi ekranında görünmeyecek ancak "İstatistikler" ekranında geçmiş süreler hesaplanmaya devam edecek.

### 1.3 Uygulama Durum Yönetimi ve Zaman Kaydı
Uygulamanın iki farklı zaman kayıt yeteneği olacak:
1. **Aktif Kronometre:** Arka planda Android "Foreground Service" çalışacak, böylece işletim sistemi sayacı durdurmayacak. Durum çubuğunda kalıcı bildirim olacak.
2. **Manuel Ekleme:** Kullanıcı unuttuğu oturumlar için ("Dün 2 saat okuma yaptım" vb.) geçmiş tarihe manuel giriş yapabilecek.

---

## 2. Tasarım Dili (UI / UX Kararları)

Tasarım için sağlanan HTML iskeletlerine dayalı modern ve "dark-mode" ağırlıklı bir arayüz dili benimsenecektir.

### 2.1 Renk Paleti ve Tipografi
*   **Ana Vurgu Rengi (Primary):** `#13ec5b` (Canlı/Neon Yeşil)
*   **Arka Plan (Zemin):** Koyu Mod için `#102216` (Çok Koyu Yeşil/Siyah), Açık mod için `#f6f8f6`
*   **Kart ve Yüzeyler (Surface):** `#162e1e` / `#1a3325` gibi tonlar.
*   **Tipografi:** `Inter` font ailesi (Okunabilirlik ve modern görünüm için).
*   **İkonografi:** `Material Symbols Outlined`.

### 2.2 Temel Ekranlar ve Bileşenleri

**1. Ana Dashboard (`dashboard.html` Karşılığı)**
*   **Giriş:** "Welcome back" mesajı ve toplam günlük takip süresi vurgusu (Örn: 4h 23m).
*   **Hızlı Başlat:** Dev boyutlu "Start Focus Session" butonu ve hemen altında "Hızlı Başlangıç" (Quick Start) kategori ızgarası (Study, Gym, Coding, vs.).
*   **Geçmiş:** En altta "Recent History" listesi (Fotoğraf destekli küçük kartlar).

**2. Aktif Zamanlayıcı (`active_timer.html` Karşılığı)**
*   **Görsel:** Arkasında primary renk ile hafif bir parlama (glow) efekti olan devasa sayaç (Saat : Dakika : Saniye).
*   **Medya:** Oturum esnasında veya bitiminde **"Add Photo"** (Fotoğraf Ekle) butonu.
*   **Kontroller:** İptal/Durdur (Stop - Kırmızı) ve Duraklat (Pause - Yeşil/Primary).

**3. Günlük Hedefler (`daily_goals_progress.html` Karşılığı)**
*   **Özet Kartları:** "Total Focused" ve "Goals Met" (Örn: 2/4 hedef tamamlandı).
*   **Hedef Listesi:** Her kategori için yüzdelik ilerleme çubukları (Progress Bar). Tamamlanmaya yaklaşanlara ekstra UI vurgusu.

**4. Haftalık İstatistikler (`weekly_statistics_overview.html` Karşılığı)**
*   **Export Butonları:** Veriyi CSV ve PDF olarak dışa aktarmak için butonlar.
*   **Aktivite Kırılımı:** Tam ortada, renkli "Pie Chart" (Pasta Grafik) ve yanında yüzdelik açıklamalar.
*   **Günlük Trendler:** Pazartesiden Pazara günlük çalışma miktarlarını gösteren "Bar Chart" (Çubuk Grafik).
*   **Alt Detaylar:** En çok vakit ayrılan projeler/kategoriler (Top Projects) için drill-down listesi.

---

## 3. Geliştirme Yol Haritası (Güncel)

1.  **Aşama 1: Temel Android & Room Kurulumu**
    *   Projenin Jetpack Compose ile oluşturulması.
    *   Room DB tablolarının (Soft-Delete dahil) kurulması.
2.  **Aşama 2: UI Kodlaması (HTML'den Compose'a)**
    *   Dashboard, Active Timer, Goals, ve Stats ekranlarının Compose ile sadık kalınarak çizilmesi.
3.  **Aşama 3: Kronometre ve Servisler**
    *   Foreground Service ile zamanlayıcının bağlanması.
    *   Manuel zaman ekleme popup'ının yapılması.
4.  **Aşama 4: İstatistik ve Grafikler**
    *   Room DB'den gelen verilerin Pie Chart ve Bar Chart olarak görselleştirilmesi.
5.  **Aşama 5: Firebase ve Auth (İleri Aşama)**
    *   Google Login eklenmesi.
    *   Firestore ile ücretsiz senkronizasyon (cihazlar arası yedekleme).
