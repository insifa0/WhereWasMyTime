# WhereWasMyTime - V1 Geri Bildirimleri ve Geliştirme Planı

## 🐛 Hatalar ve Çalışmayan Aksiyonlar (Bug Fixes)
- **Kategori Seçimi/Kronometre Uyuşmazlığı:** Ana ekranda hızlı başlattan kategori seçip kronometreyi başlattıktan sonra, başka bir kategori seçildiğinde kronometre hâlâ önceki kategoriyi gösteriyor. (Acil düzeltme)
- **Çalışmayan Butonlar:** "Oturum Başlat" ve "Kategori Seç ve Başla" butonları şu an işlevsiz, çalışır hale getirilmeli.
- **Fotoğraf Ekleme:** "Fotoğraf Ekle" butonu henüz çalışmıyor, işlevselliği eklenmeli.

## 🚀 Yeni Özellik İhtiyaçları (Feature Requests)
- **Tüm Çalışmalar (Geçmiş/İstatistik) Sayfası:** Bugüne kadar yapılan tüm çalışmaların (toplam çalışma süreleri, geçmiş vb.) toplu olarak görülebileceği bir alan/sayfa eklenmeli.
- **Aktivite Notlarının Görüntülenmesi:** Manuel çalışma eklerken girilen notlar kaydediliyor ancak uygulamada hiçbir yerde gösterilmiyor. Aktivitelerin listelendiği alana veya detay sayfasına not içeriği eklenmeli.
- **Yeni Hedef Ekleme:** Hedefler sayfasına yeni hedef oluşturabilmek için bir `+` (ekle) butonu koyulmalı.
- **Yanlış Aktiviteleri Silme İşlevi:** Son aktiviteler listesinde, yanlışlıkla eklenen çalışmaları silebilmek için bir silme butonu/işlevi eklenmeli.
- **Otomatik Filtreleme (Kısa Aktiviteler):** 1 dakikanın altında kalan aktiviteler (yanlışlıkla başlatılıp durdurulmuş sayılacağından) otomatik olarak silinmeli/kaydedilmemeli.

## 💄 Kullanıcı Deneyimi ve Arayüz İyileştirmeleri (UX/UI Enhancements)
- **Kişiselleştirilmiş Karşılama:** Ana ekrandaki "hoş geldin Dashboard" genel mesajı yerine, Google entegrasyonundan çekilen kullanıcı adı (örn: "Hoş geldin, [İsim]") gösterilmeli.
- **Aktif Çalışma Görünürlüğü:** Kronometre başlatıldığında; ana ekranda **aktif çalışmanın adı**, **geçen süre** ve (varsa) **hedef süre** açık/net bir şekilde görülebilmeli.

---

## 🔮 V2: Daha Sonradan Eklenebilecek Özellikler (Backlog)
### 1. Sosyal ve Rekabetçi Özellikler
- **Kullanıcı Etkileşimi:** Google ile giriş yapan kullanıcıların birbirlerinin ne kadar çalıştığını görebilmesi (Liderlik tablosu veya arkadaş sistemi gibi).

### 2. Gelişmiş Kategori Sistemi (Alt Kategoriler)
Ana kategorilerin altında spesifik alt kategoriler oluşturulabilmeli. Bu alt kategoriler kullanıcı tarafından **eklenebilir** ve **silinebilir** yapıda olmalı.

*Örnek Hiyerarşi:*
- **Ders:** Matematik, Fizik, Kimya, Biyoloji
- **İş:** Toplantı, Rapor, Sunum
- **Hobi:** Kitap Okuma, Film İzleme, Oyun Oynama
- **Spor:** Koşu, Yüzme, Bisiklet
- **Diğer:** Yemek, Uyku, Dinlenme
