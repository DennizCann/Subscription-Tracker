# Substracktion

Dijital aboneliklerinizi tek ekranda toplayın: ülkeye göre para birimi, popüler servislerden hızlı ekleme ve **Groq** ile plan / fiyat önerileri. Veriler cihazda **Room** ile saklanır; düzenleme ve silme desteklenir.

---

## Özellikler

| | |
| --- | --- |
| **Liste** | Kayıtlı üyelikler, tutar ve ödeme dönemi (haftalık / aylık / yıllık) |
| **Ülke** | Üst çubuktan ülke seçimi; varsayılan para birimi |
| **Ekleme** | Katalogdan servis seçimi → AI plan önerileri veya manuel plan girişi |
| **Düzenleme** | Karta dokununca aynı plan ekranı; kayıt güncellenir |
| **Silme** | Karta **basılı tutunca** onay diyaloğu ile silme |
| **Kalıcılık** | Room veritabanı (`subscriptions` tablosu) |

> **Uyarı:** AI tarafından üretilen plan adları ve fiyatlar tahmindir. Kritik kararlar için resmi site veya faturanızı kontrol edin.

---

## Teknoloji yığını

- **Kotlin** · **Jetpack Compose** · **Material 3**
- **Navigation Compose**
- **Room** + **KSP** (şema derlemesi)
- **Retrofit** · **OkHttp** · **Gson** — Groq uyumlu API
- **ViewModel** · **StateFlow** · **Kotlin Coroutines**

**Minimum SDK:** 24 · **Hedef / derleme:** 36

---

## Kurulum

### Gereksinimler

- [Android Studio](https://developer.android.com/studio) (Ladybug veya üzeri önerilir)
- **JDK 11** (projede `jvmTarget = 11`)

### Groq API anahtarı

Proje kökünde `local.properties` dosyasına (bu dosya **Git’e eklenmemelidir**) şunu ekleyin:

```properties
groq.api.key=YOUR_GROQ_API_KEY_HERE
```

Derleme sırasında değer `BuildConfig.GROQ_API_KEY` olarak uygulamaya gömülür. Anahtarı repoya veya ekran görüntüsüne **yüklemediğinizden** emin olun.

### Çalıştırma

```text
./gradlew assembleDebug
```

veya Android Studio’da **Run** ile `app` modülünü çalıştırın.

---

## Mimari (kısa)

```text
ui/          Compose ekranları, navigasyon, ViewModel’ler
domain/      Modeller, kataloglar, para birimi formatlama
data/        Room (entity, DAO, DB), Groq plan repository
```

- **SubstracktionViewModel** — abonelik listesi, ekleme, güncelleme, silme
- **PlanSuggestionViewModel** — Groq’dan plan listesi veya hata / manuel akış

---

## Proje yapısı (özet)

```text
app/src/main/java/com/example/substracktion/
├── data/local/          # Room
├── data/repository/     # GroqPlanRepository
├── domain/              # model, catalog, formatter
└── ui/                  # screens, navigation, theme, viewmodel
```

---

## Katkı ve lisans

İyileştirme önerileri ve PR’lar memnuniyetle karşılanır. Lisans henüz tanımlanmadıysa repoya uygun bir `LICENSE` dosyası eklemeniz önerilir.

---

**Substracktion** — abonelikleri takip etmek için küçük, odaklı bir Android uygulaması.
