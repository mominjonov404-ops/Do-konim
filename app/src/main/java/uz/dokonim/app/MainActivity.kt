package uz.dokonim.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Green = Color(0xFF16A36A)
private val DarkBg = Color(0xFF10151D)
private val DarkCard = Color(0xFF1A2330)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DokonimApp()
        }
    }
}

@Composable
fun DokonimApp() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("dokonim", 0)
    }

    var phone by remember {
        mutableStateOf(prefs.getString("phone", "") ?: "")
    }
    var loggedIn by remember {
        mutableStateOf(prefs.getBoolean("logged", false))
    }
    var isDark by remember {
        mutableStateOf(prefs.getBoolean("dark", true))
    }
    var language by remember {
        mutableStateOf(prefs.getString("lang", "UZ") ?: "UZ")
    }
    var page by remember { mutableStateOf("home") }
    var products by remember {
        mutableIntStateOf(prefs.getInt("products", 0))
    }
    var sales by remember {
        mutableLongStateOf(prefs.getLong("sales", 0L))
    }
    var expenses by remember {
        mutableLongStateOf(prefs.getLong("expenses", 0L))
    }
    var debts by remember {
        mutableLongStateOf(prefs.getLong("debts", 0L))
    }
    var showAdd by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var actionType by remember { mutableStateOf("sale") }

    val bg = if (isDark) DarkBg else Color(0xFFF4F6F8)
    val card = if (isDark) DarkCard else Color.White
    val fg = if (isDark) Color.White else Color(0xFF17212B)
    val muted = if (isDark) Color(0xFF9BA8B8) else Color(0xFF64748B)

    MaterialTheme(
        colorScheme = if (isDark) {
            darkColorScheme(primary = Green, background = bg, surface = card)
        } else {
            lightColorScheme(primary = Green, background = bg, surface = card)
        }
    ) {
        if (!loggedIn) {
            AuthScreen(
                isDark,
                language,
                { value ->
                    isDark = value
                    prefs.edit().putBoolean("dark", value).apply()
                },
                { value ->
                    language = value
                    prefs.edit().putString("lang", value).apply()
                }
            ) { entered ->
                val normalized = entered.filter { it.isDigit() }
                if (normalized.length >= 9) {
                    phone = if (entered.startsWith("+")) entered else "+$entered"
                    loggedIn = true
                    prefs.edit()
                        .putString("phone", phone)
                        .putBoolean("logged", true)
                        .apply()
                }
            }
        } else {
            Scaffold(
                containerColor = bg,
                bottomBar = {
                    NavigationBar(containerColor = card) {
                        val items = listOf(
                            "home" to Icons.Default.Home,
                            "products" to Icons.Default.ShoppingCart,
                            "sales" to Icons.Default.ShoppingCart,
                            "settings" to Icons.Default.Settings
                        )

                        items.forEach { (key, icon) ->
                            NavigationBarItem(
                                selected = page == key,
                                onClick = { page = key },
                                icon = { Icon(icon, contentDescription = null) },
                                label = {
                                    Text(
                                        when (key) {
                                            "home" -> tr(language, "Bosh sahifa", "Главная", "Home")
                                            "products" -> tr(language, "Mahsulotlar", "Товары", "Products")
                                            "sales" -> tr(language, "Savdo", "Продажи", "Sales")
                                            else -> tr(language, "Sozlamalar", "Настройки", "Settings")
                                        }
                                    )
                                }
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (page == "sales" || page == "products") {
                        FloatingActionButton(
                            onClick = {
                                actionType = if (page == "sales") "sale" else "product"
                                amountText = ""
                                showAdd = true
                            },
                            containerColor = Green
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bg)
                        .padding(padding)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Do‘konim",
                                    color = fg,
                                    fontSize = 27.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(phone, color = muted, fontSize = 12.sp)
                            }

                            Surface(shape = RoundedCornerShape(14.dp), color = card) {
                                Text(
                                    if (isDark) "☾" else "☀",
                                    Modifier.clickable {
                                        isDark = !isDark
                                        prefs.edit().putBoolean("dark", isDark).apply()
                                    }.padding(12.dp),
                                    color = fg
                                )
                            }
                        }
                    }

                    when (page) {
                        "home" -> {
                            item {
                                MetricCard("Bugungi savdo", money(sales), "point-of-sale", card, fg, muted)
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(Modifier.weight(1f)) {
                                        MetricCard("Xarajatlar", money(expenses), "", card, fg, muted)
                                    }
                                    Box(Modifier.weight(1f)) {
                                        MetricCard("Qarzlar", money(debts), "", card, fg, muted)
                                    }
                                }
                            }
                            item {
                                MetricCard("Mahsulotlar", products.toString(), "inventory", card, fg, muted)
                            }
                            item { SectionTitle("Tezkor amallar", fg) }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    QuickButton("Savdo qo‘shish", card, fg) {
                                        actionType = "sale"
                                        showAdd = true
                                    }
                                    QuickButton("Xarajat", card, fg) {
                                        actionType = "expense"
                                        showAdd = true
                                    }
                                }
                            }
                            item {
                                QuickButton("Qarz qayd etish", card, fg) {
                                    actionType = "debt"
                                    showAdd = true
                                }
                            }

                            if (phone.filter { it.isDigit() }.takeLast(12) == "998941771247") {
                                item { SectionTitle("Administrator", fg) }
                                item {
                                    AdminCard(card, fg, muted, products, sales, expenses, debts)
                                }
                            }
                        }

                        "products" -> {
                            item { SectionTitle("Mahsulotlar", fg) }
                            item {
                                Text("Mahsulotlar soni: $products", color = muted)
                            }
                            item {
                                QuickButton("Yangi mahsulot qo‘shish", card, fg) {
                                    actionType = "product"
                                    showAdd = true
                                }
                            }
                        }

                        "sales" -> {
                            item { SectionTitle("Savdo", fg) }
                            item {
                                MetricCard("Jami qayd etilgan savdo", money(sales), "", card, fg, muted)
                            }
                            item {
                                QuickButton("Yangi savdo", card, fg) {
                                    actionType = "sale"
                                    showAdd = true
                                }
                            }
                        }

                        else -> {
                            item { SectionTitle("Sozlamalar", fg) }
                            item {
                                QuickButton("Til: $language", card, fg) {
                                    language = when (language) {
                                        "UZ" -> "RU"
                                        "RU" -> "EN"
                                        else -> "UZ"
                                    }
                                    prefs.edit().putString("lang", language).apply()
                                }
                            }
                            item {
                                QuickButton(
                                    if (isDark) "Yorug‘ rejim" else "Tungi rejim",
                                    card,
                                    fg
                                ) {
                                    isDark = !isDark
                                    prefs.edit().putBoolean("dark", isDark).apply()
                                }
                            }
                            item {
                                QuickButton("Hisobdan chiqish", card, fg) {
                                    loggedIn = false
                                    prefs.edit().putBoolean("logged", false).apply()
                                }
                            }
                            item {
                                Text("Do‘konim MVP 1.0.0", color = muted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            if (showAdd) {
                AlertDialog(
                    onDismissRequest = { showAdd = false },
                    title = {
                        Text(
                            when (actionType) {
                                "sale" -> "Savdo qo‘shish"
                                "expense" -> "Xarajat qo‘shish"
                                "debt" -> "Qarz qo‘shish"
                                else -> "Mahsulot qo‘shish"
                            }
                        )
                    },
                    text = {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { newValue: String ->
                                amountText = newValue.filter { ch -> ch.isDigit() }
                            },
                            label = { Text("Summa yoki mahsulot soni") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val value = amountText.toLongOrNull() ?: 0L

                            when (actionType) {
                                "sale" -> {
                                    sales += value
                                    prefs.edit().putLong("sales", sales).apply()
                                }
                                "expense" -> {
                                    expenses += value
                                    prefs.edit().putLong("expenses", expenses).apply()
                                }
                                "debt" -> {
                                    debts += value
                                    prefs.edit().putLong("debts", debts).apply()
                                }
                                else -> {
                                    products += value.toInt()
                                    prefs.edit().putInt("products", products).apply()
                                }
                            }
                            showAdd = false
                        }) {
                            Text("Saqlash")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAdd = false }) {
                            Text("Bekor qilish")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthScreen(
    dark: Boolean,
    lang: String,
    onDark: (Boolean) -> Unit,
    onLang: (String) -> Unit,
    onContinue: (String) -> Unit
) {
    var phone by remember { mutableStateOf("+998") }
    var password by remember { mutableStateOf("") }

    val fg = if (dark) Color.White else Color(0xFF17212B)

    Column(
        Modifier
            .fillMaxSize()
            .background(if (dark) DarkBg else Color(0xFFF4F6F8))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Do‘konim",
            color = Green,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Do‘koningiz boshqaruvi bir joyda",
            color = fg,
            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telefon raqami") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Parol (demo)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )

        Text(
            "SMS tasdiqlash hozircha o‘chirilgan. MVP sinov rejimida.",
            color = if (dark) Color.LightGray else Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Button(
            onClick = {
                if (password.length >= 4) onContinue(phone)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            Text("Kirish / Ro‘yxatdan o‘tish", color = Color.White)
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Til: $lang",
                color = fg,
                modifier = Modifier.clickable {
                    onLang(
                        when (lang) {
                            "UZ" -> "RU"
                            "RU" -> "EN"
                            else -> "UZ"
                        }
                    )
                }
            )
            Text(
                if (dark) "Light mode" else "Dark mode",
                color = fg,
                modifier = Modifier.clickable { onDark(!dark) }
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: String,
    card: Color,
    fg: Color,
    muted: Color
) {
    Surface(
        color = card,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, color = muted, fontSize = 13.sp)
            Text(
                value,
                color = fg,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(t: String, c: Color) {
    Text(t, color = c, fontSize = 19.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun QuickButton(
    t: String,
    card: Color,
    fg: Color,
    onClick: () -> Unit
) {
    Surface(
        color = card,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "+",
                color = Green,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                t,
                color = fg,
                modifier = Modifier.padding(start = 10.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AdminCard(
    card: Color,
    fg: Color,
    muted: Color,
    products: Int,
    sales: Long,
    expenses: Long,
    debts: Long
) {
    Surface(
        color = card,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("ADMIN PANEL", color = Green, fontWeight = FontWeight.Bold)
            Text("Mahalliy demo statistikasi", color = muted, fontSize = 12.sp)
            Text("Mahsulotlar: $products", color = fg)
            Text("Savdo: ${money(sales)}", color = fg)
            Text("Xarajatlar: ${money(expenses)}", color = fg)
            Text("Qarzlar: ${money(debts)}", color = fg)
        }
    }
}

private fun money(v: Long): String =
    "%,d so‘m".format(v).replace(',', ' ')

private fun tr(l: String, uz: String, ru: String, en: String) =
    when (l) {
        "RU" -> ru
        "EN" -> en
        else -> uz
    }
