# WhereWasMyTime ⏳

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)

WhereWasMyTime, gün içinde zamanınızı nereye harcadığınızı takip etmenizi sağlayan, modern ve karanlık temalı (Dark Mode) bir zaman yönetimi ve odaklanma uygulamasıdır. Material Design 3 prensipleriyle tasarlanmış olup Jetpack Compose kullanılarak geliştirilmiştir.

## 🚀 Özellikler

- **⏱️ Aktif Zamanlayıcı (Foreground Service):** Uygulamadan çıksanız bile bildirim çubuğunda saniyesi saniyesine (HH:MM:SS) saymaya devam eden kararlı kronometre.
- **🏷️ Kategori Yönetimi:** Çalışma, Spor, Dinlenme gibi sınırsız kategori ekleyebilir, her birine özel yatay paletten (12 farklı renk) renk seçebilirsiniz. Arşivleme (Soft-Delete) desteği mevcuttur.
- **🎯 Günlük Hedefler (Gamification):** Her kategori için günlük dakika/saat hedefi belirleme ve ilerleme çubukları ile yüzdelik takibi.
- **📊 Raporlar ve Grafikler:** 
  - Özel yapım Canvas Bar Chart ile haftalık ve aylık bazda gün-gün zaman dağılımı.
  - Yatay yüzde çubukları ile kategori bazlı "Activity Breakdown".
- **✍️ Manuel Zaman Girişi:** Unutulan veya geçmişteki bir oturumu tarih, başlangıç saati ve süre (dk) girerek sonradan ekleyebilme.
- **⚙️ Özelleştirme ve Güvenlik:** Aydınlık/Karanlık tema geçişi, tek tuşla güvenli veri sıfırlama (Wipe Data) ve bildirim izni yönetimi.

## 🛠️ Teknolojiler ve Mimari

Uygulama, modern Android geliştirme standartlarına uygun olarak inşa edilmiştir:

*   **Dil:** Kotlin
*   **Arayüz (UI):** Jetpack Compose (Material 3)
*   **Mimari:** MVVM (Model-View-ViewModel) + Repository Pattern
*   **Veritabanı:** Room Database (Coroutines & Flow ile Reaktif Mimarî)
*   **Navigasyon:** Navigation Compose
*   **Arka Plan İşlemleri:** Foreground Service (Android 14+ uyumlu)

## 📱 Ekran Görüntüleri 
*(Eklenecek)*

## ⚙️ Kurulum (Geliştiriciler İçin)

1. Depoyu klonlayın:
   ```bash
   git clone https://github.com/kullaniciadi/WhereWasMyTime.git
   ```
2. Android Studio'yu açın ve projeyi (klasörü) seçin.
3. Gradle senkronizasyonunun bitmesini bekleyin.
4. Fiziksel bir cihazda veya Emülatörde projeyi çalıştırın (Run). *Not: Minimum SDK 26 (Android 8.0) gerektirir.*
