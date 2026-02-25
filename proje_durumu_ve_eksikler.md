# WhereWasMyTime: Proje Durumu ve Eksikler Raporu

Şu ana kadar uygulamanın **büyük omurgası (Faz 1-7)** başarıyla tamamlandı. Aşağıda sistemin genel durumunu, tespit edilen eksiklikleri ve yapılabilecek iyileştirmeleri detaylı bir şekilde görebilirsin.

---

## 🛠️ Eksikler ve Yapılacak İşlemlerin Detayları

Mevcut kodu incelediğimde göze çarpan 6 temel noksan parça bulunuyor. Bunlar uygulamanın "kullanılabilir bir araç" olmaktan çıkıp "tam teşekküllü bir ürün" olmasına engel olan özelliklerdir.

### 1. Ayarlar (Settings) Ekranı
Şu an Bottom Navigation'da "Ayarlar" sekmesi var ancak tıkladığında "Ayarlar — Yakında" yazan boş bir sayfa çıkıyor. Burayı interaktif, veri sıfırlama işlemlerini güvenli yapabileceğin bir kontrol paneline dönüştürmeliyiz.
* **Nasıl Yapılacak?**
  * `SettingsScreen.kt` dosyası ve `SettingsViewModel` oluşturulacak.
  * **Uygulama Teması:** "Sadece Karanlık", "Sadece Aydınlık" veya "Sistem Varsayılanı" seçeneği (Shared Preferences veya Datastore üzerinden) eklenecek.
  * **Veri Yönetimi Tıklaması:** "Tüm Verileri Sil" butonu yapılacak. Kullanıcı tıkladığında bir AlertDialog ("Emin misiniz? Tüm geçmiş silinecek") çıkacak. Onaylanırsa Room DB'deki Session ve Category tabloları temizlenip, prepopulate veriler (varsayılan kategoriler) geri yüklenecek.
  * **İzin Kontrolü:** Uygulama içi (In-App) menü ayarları eklenecek. Örneğin Android Ayarlarına gidip "Tam Ekran Bildirim" izinlerini kontrol et butonu konulacak.

### 2. Kategori Yönetimi (Ekle / Sil / Düzenle)
Şu an DB ilk oluşturulduğunda "Ders, Programlama, Spor..." gibi 5 varsayılan kategori ekleniyor. Ancak kullanıcının uygulamayı ihtiyacına göre özelleştirmek için yeni kategori ekleme şansı yok.
* **Nasıl Yapılacak?**
  * Yeni bir arayüz rotası açılacak: `ManageCategoriesScreen`.
  * **Ekleme İşlemi:** Bir Float Action Button (FAB) ile veya `HomeScreen` Grid'inin en sonuna "Yeni Kategori Ekle (+)" kartı basılarak tetiklenecek. 
  * Kullanıcıdan string olarak isim istenecek ve renk paleti için `LazyRow`'da yuvarlak renk seçici (örn: 10 farklı hex kodu) sunulacak.
  * **Silme İşlemi:** Kategorilerin üstüne uzun basınca "Sil" menüsü çıkacak. Veritabanında kategoriler *tamamen silinmeyecek* (`DELETE FROM`), bunun yerine `isArchived = true` yapılacak (Soft Delete). Böylece önceki aylara ait raporlardaki "Spor" verisi kaybolmayacak, sadece Ana Ekranda "Hızlı Başlat" butonundan kalkacak.

### 3. Oturum (Session) Silme / Düzenleme (Yanlış Kayıtları Düzeltme)
"Son Aktiviteler" listesinde veya Raporlarda bir oturum yanlış girildiyse (örneğin manuel girişte sabah 09:00 yerine yanlıkla 21:00 girildiyse) silme veya düzeltme imkanı yok. Timer hatalı durdurulmuş olabilir.
* **Nasıl Yapılacak?**
  * **Swipe-to-Dismiss (Kaydırarak Sil):** `HomeScreen` altındaki Son Aktiviteler listesindeki (`RecentSessionItem`) kartlar, Compose'un `SwipeToDismissBox` componenti içine alınacak. Sağa kaydırıldığında kırmızı bir "Çöp Kutusu" ikonu belirecek ve bırakıldığında o ID'li `SessionEntity` anında `deleteSession(id)` ile silinecek.
  * *(Opsiyonel)* Silinince ekrana bir "Geri Al (Undo)" butonu içeren SnackBar çıkarılabilir.
  * Tıklanıldığında bir BottomSheet açılarak "Başlangıç saati" ve "Süre" değerleri değiştirilebilecek (Update Session).

### 4. Fotoğraf Ekleme (Photo Attachment) Özelliği
Veritabanında `SessionEntity` modelinde, yapılan işi kanıtlamak veya hatıra bırakmak için `photoPath` isminde bir string sütunu koymuştuk. Aktif Timer ekranında da "Fotoğraf Ekle" butonu tasarımı var ancak tıkladığında bir şey yapmıyor.
* **Nasıl Yapılacak?**
  * Android'in standart `ActivityResultContracts.TakePicture` (Kamera açma intent'i) çalıştırılacak.
  * Çekilen fotoğraf uygulamanın Cache veya Files klasörüne (`requireContext().filesDir`) bir timestamp ID'siyle (örn: `IMG_20261125_120400.jpg`) kaydedilecek.
  * Bu dosyanın yolu (Absolute Path), aktif oturum durdurulduğunda `SessionEntity`'nin `photoPath` kolonuna insert edilecek.
  * **Görüntüleme:** "Raporlar" veya "Son Aktiviteler" tıklanıp oturum detayına girildiğinde Coil veya Glide kütüphanesi yardımıyla o fotoğraf gösterilecek.

### 5. Raporlar Ekranında Dışa Aktar (Export PDF/CSV)
Kullanıcıların kendi verilerine sahip olmasını sağlamak (Örn: müşteriye çalışma saatlerini göstermek için fatura niyetine veya patronuna göndermek için rapor). Haftalık veya aylık bar grafiklerini çizsek bile somut bir liste lazım.
* **Nasıl Yapılacak?**
  * Raporlar ekranının sağ üst köşesine bir İndirme (`Icons.Default.Download`) ikonu konacak.
  * Tıkladığında "CSV Olarak İndir" seçeneği sunulacak.
  * Bu buton `ReportsViewModel`'a bir event atacak. Viewmodel, seçili tarih aralığındaki (`startMs` - `endMs`) oturumları çekecek.
  * Veriler bir Text dosyasında "Oturum ID,Kategori,Başlangıç Saati,Süre (dk)" şeklinde virgülle ayrılıp oluşturulacak. Ardından Android'in paylaşım (Share Intent) diyaloğu açılarak WhatsApp, Mail veya Dosyalar uygulamasına gönderilmesi sağlanacak.

### 6. Firebase ve Senkronizasyon (Faz 8)
Şu an uygulamanın veritabanı "yerel" (Local - Room DB). Yani kullanıcı eğer telefonundan uygulamayı silerse, veya telefonu bozulursa şimdiye kadar tuttuğu 1 yıllık emek verdiği çalışma veya ders geçmişi tamamen silinir.
* **Nasıl Yapılacak?**
  * **Kimlik Doğrulama:** Sağlam bir Google Sign-In backend'i bağlanacak. Kullanıcı tek tuşla hesabını bağlayacak.
  * **Bulut Veritabanı:** Firestore (NoSQL) kullanılacak. Local `SessionEntity` ve `CategoryEntity` nesneleri düzenli aralıklarla (veya her kapanışta) kullanıcının Firestore'daki dokümanına (Collection: `users/uid/sessions`) senkronize edilecek.
  * Bu sayede "buluttan veri çekme", aynı hesaba sahip başka bir tabletten bakıldığında verilerin aynı görünmesi (Cross-device sync) problemleri çözülecek.

---
Bu maddeler içerisinde en hızlı tamamlanabilecek ve uygulamanın kullanılabilirliğini anında artıracak ilk iki madde **"Kategori Ekleme/Düzenleme"** ve kayıt hataları için **"Oturum Silme (Swipe-to-Dismiss)"** dir.
