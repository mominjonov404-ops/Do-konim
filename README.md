# Do‘konim Android MVP

Kotlin + Jetpack Compose asosidagi boshlang‘ich Android loyiha.

## APK build
GitHub repositoryga barcha fayllarni joylang. `Actions` bo‘limidan **Build DoKonim APK** workflow'ini ishga tushiring. Build tugagach, `Dokonim-debug-apk` artifactini yuklab oling.

## Muhim MVP cheklovlari
- Hozirgi kirish ekrani faqat lokal demo: telefon raqami va kamida 4 belgili parol talab qilinadi, lekin parol serverda tekshirilmaydi.
- Admin panel telefon raqami bilan lokal ko‘rinadi. Bu xavfsiz haqiqiy admin autentifikatsiyasi emas.
- Ma'lumotlar faqat shu telefonda saqlanadi; ko‘p foydalanuvchili boshqaruv, SMS, bulutli zaxira va real obuna hali ulanmagan.
- Haqiqiy ishga tushirishdan oldin Supabase Auth/DB va server tomonda admin roli tekshiruvini ulash zarur. Admin maxfiy kodi hech qachon ilova ichida saqlanmasligi kerak.
