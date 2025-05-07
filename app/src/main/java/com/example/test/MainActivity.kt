package com.example.test

import android.graphics.HardwareRenderer
import android.graphics.Paint.Align
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.text.Layout
import android.view.ViewDebug.IntToString
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import coil.compose.rememberAsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AccountCircle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.ui.text.style.TextOverflow
import retrofit2.http.POST
import retrofit2.http.*
import android.app.Activity
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import android.util.LruCache
import androidx.compose.material.icons.filled.HowToReg
import kotlin.math.log
import android.content.Context

class Product(
    var id: Int = 0,
    var name: String = "",
    var price: Double = 0.0,
    var image_url: String = "",
    var count: Int = 1,
    val categoryId: Int = 0,
    val description: String = ""
)

data class AuthRequest(
    val login: String,
    val password: String
)

data class User(
    val id: String,
    val login: String,
    val password: String
)

data class AuthResponse(
    val message: String? = null,
    val user: User? = null,
    val error: String? = null
)

interface AuthService {
    @POST("register")
    fun registerUser(@Body authRequest: AuthRequest): Call<AuthResponse>

    @POST("login")
    fun loginUser(@Body authRequest: AuthRequest): Call<AuthResponse>
}

// Интерфейс для работы с API
interface ApiService {
    @POST("add")
    fun createProduct(@Body product: Product): Call<Product>
}

fun getRetrofitInstance(): Retrofit {
    return Retrofit.Builder()
        .baseUrl("http://93.125.42.181:3001/") // Адрес вашего сервера
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun sendRegistrationToServer(authRequest: AuthRequest, LoginStatus: MutableState<Int> ) {
    val apiService = getRetrofitInstance().create(AuthService::class.java)
    apiService.registerUser(authRequest).enqueue(object : Callback<AuthResponse> {
        override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
            if (response.isSuccessful) {
                val authResponse = response.body()
                println("Регистрация успешна: $authResponse")
                LoginStatus.value = 1
            } else {
                println("Ошибка при регистрации: ${response.errorBody()?.string()}")
                LoginStatus.value = -1
            }
        }
        override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
            println("Ошибка сети при регистрации: ${t.message}")
        }
    })
}

fun sendLoginToServer(authRequest: AuthRequest, LoginStatus: MutableState<Int>) {
    val apiService = getRetrofitInstance().create(AuthService::class.java)
    apiService.loginUser(authRequest).enqueue(object : Callback<AuthResponse> {
        override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
            if (response.isSuccessful) {
                val authResponse = response.body()
                println("Успешный вход: $authResponse")
                // Здесь можно сохранить данные пользователя или переключить экран
                LoginStatus.value = 1
            } else {
                println("Ошибка при входе: ${response.errorBody()?.string()}")
                LoginStatus.value = -1
            }
        }
        override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
            println("Ошибка сети при входе: ${t.message}")
        }
    })
}

fun sendProductToServer(product: Product) {
    val apiService = getRetrofitInstance().create(ApiService::class.java)

    apiService.createProduct(product).enqueue(object : Callback<Product> {
        override fun onResponse(call: Call<Product>, response: Response<Product>) {
            if (response.isSuccessful) {
                // Товар успешно добавлен
                println("Товар успешно добавлен: ${response.body()}")
            } else {
                // Ошибка на сервере
                println("Ошибка при добавлении товара: ${response.errorBody()}")
            }
        }

        override fun onFailure(call: Call<Product>, t: Throwable) {
            // Ошибка при выполнении запроса
            println("Ошибка сети: ${t.message}")
        }
    })
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreateUI()
        }
    }
}

@Composable
fun RegistrationScreen(isFirstlaunch : MutableState<Boolean>) {

    var RegisStatus = remember { mutableStateOf(0) }

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }

    val visualTransformation = if (isPasswordVisible) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }


    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()) {

        Text(text = "Регистрация аккаунта", fontFamily = MainFont, fontSize = 35.sp)

        Spacer(modifier = Modifier.height(35.dp))

        Text(text = "Логин", fontFamily = MainFont)

        TextField(
            leadingIcon = { Icon(Icons.Filled.Login, contentDescription = null) },
            modifier = Modifier
                .padding(15.dp, 5.dp)
                .size(270.dp, 50.dp)
            ,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                focusedLeadingIconColor = Color.Black,
                unfocusedLeadingIconColor = Color.Black,
            ),
            shape = RectangleShape, // Убираем скругление
            value = login,
            onValueChange = { login = it }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Пароль", fontFamily = MainFont)
        Row( verticalAlignment = Alignment.CenterVertically, // Выравниваем элементы по вертикали
            modifier = Modifier.fillMaxWidth())
        {
            TextField(
                leadingIcon = { Icon(Icons.Filled.Password, contentDescription = null) },
                modifier = Modifier
                    .padding(15.dp, 45.dp)
                    .size(270.dp, 50.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    focusedLeadingIconColor = Color.Black,
                    unfocusedLeadingIconColor = Color.Black,
                ),
                shape = RectangleShape, // Убираем скругление
                value = password,
                onValueChange = { password = it },
                visualTransformation = visualTransformation
            )

            IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible },
            ) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle password visibility"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (login.isNotEmpty() && password.isNotEmpty()) {
                    val request = AuthRequest(login, password)
                    sendRegistrationToServer(request, RegisStatus)
                    successMessage = "Запрос на регистрацию отправлен"
                    errorMessage = ""
                } else {
                    errorMessage = "Поля логина и пароля должны быть заполнены!"
                    successMessage = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
            shape = RoundedCornerShape(3.dp)
        ) {
            Text(
                text = "Войти",
                modifier = Modifier.padding(0.dp),
                fontSize = 18.sp,
                fontFamily = MainFont,
                color = Color(255, 255, 255)
            )

        }

        Spacer(modifier = Modifier.height(16.dp))

        if (RegisStatus.value == -1)
        {
            Text(fontSize = 18.sp,
                fontFamily = MainFont, text = "Регистрация не прошла. Логин занят", color = androidx.compose.ui.graphics.Color.Red)
        }
        if (RegisStatus.value == 1)
        {
            isFirstlaunch.value = false;
            setFirstLaunchCompleted(LocalContext.current)
            Text(fontSize = 18.sp,
                fontFamily = MainFont, text = "Регистрация прошла успешно!", color = androidx.compose.ui.graphics.Color.Green)
        }

    }
}

@Composable
fun LoginScreen(isFirstlaunch : MutableState<Boolean>) {

    var LoginStatus = remember { mutableStateOf(0) }

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }

    val visualTransformation = if (isPasswordVisible) {
        VisualTransformation.None
    } else {
       PasswordVisualTransformation()
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()) {

        Text(text = "Вход в аккаунт", fontFamily = MainFont, fontSize = 35.sp)

        Spacer(modifier = Modifier.height(35.dp))

        Text(text = "Логин", fontFamily = MainFont)
        TextField(
            leadingIcon = { Icon(Icons.Filled.Login, contentDescription = null) },
            modifier = Modifier
                .padding(15.dp, 5.dp)
                .size(270.dp, 50.dp)
            ,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                focusedLeadingIconColor = Color.Black,
                unfocusedLeadingIconColor = Color.Black,
            ),
            shape = RectangleShape, // Убираем скругление
            value = login,
            onValueChange = { login = it },
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Пароль", fontFamily = MainFont)

        Row( verticalAlignment = Alignment.CenterVertically, // Выравниваем элементы по вертикали
            modifier = Modifier.fillMaxWidth())
        {
            TextField(
                leadingIcon = { Icon(Icons.Filled.Password, contentDescription = null) },
                modifier = Modifier
                    .padding(15.dp, 45.dp)
                    .size(270.dp, 50.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    focusedLeadingIconColor = Color.Black,
                    unfocusedLeadingIconColor = Color.Black,
                ),
                shape = RectangleShape, // Убираем скругление
                value = password,
                onValueChange = { password = it },
                visualTransformation = visualTransformation
            )

            IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible },
            ) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle password visibility"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (login.isNotEmpty() && password.isNotEmpty()) {
                    val request = AuthRequest(login, password)
                    sendLoginToServer(request, LoginStatus)
                    successMessage = "Запрос на вход отправлен"
                    errorMessage = ""
                } else {
                    errorMessage = "Поля логина и пароля должны быть заполнены!"
                    successMessage = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
            shape = RoundedCornerShape(3.dp)
        ) {
                Text(
                    text = "Войти",
                    modifier = Modifier.padding(0.dp),
                    fontSize = 18.sp,
                    fontFamily = MainFont,
                    color = Color(255, 255, 255)
                )

        }

        Spacer(modifier = Modifier.height(16.dp))

        if (LoginStatus.value == -1)
        {
            Text(fontSize = 18.sp,
                fontFamily = MainFont, text = "Вход не прошел. Неправильный пароль или логин", color = androidx.compose.ui.graphics.Color.Red)
        }
        if (LoginStatus.value == 1)
        {
            isFirstlaunch.value = false;
            setFirstLaunchCompleted(LocalContext.current)
            Text(fontSize = 18.sp,
                fontFamily = MainFont, text ="Вход прошел успешно!", color = androidx.compose.ui.graphics.Color.Green)
        }
    }
}
val PurpleColor = Color(0, 0, 0, 210)
val BackgroundColor =  Color(255, 255, 255, 255)
val TextColor = Color(255, 255, 255, 220)
val MainFont = FontFamily(Font(R.font.googlesans))


@Composable
fun DisplayImageFromUrl(imageUrl: String, modifier: Modifier = Modifier, alpha : Float = 1f) {
    AsyncImage(
        modifier = modifier.size(340.dp, 340.dp).alpha(alpha),
        contentScale = ContentScale.Fit,
        model = imageUrl,
        contentDescription = "im5")

}

@Composable
fun ProductList(products: List<Product>, basketOfProducts: MutableList<Product>, opened_product: MutableState<Int>) {

    val scrollState2 = rememberScrollState()

    if (opened_product.value == -1) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 элемента в линии
            modifier = Modifier.fillMaxSize().padding(0.dp, 0.dp, 0.dp, 90.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products.size) { index ->
                val product = products[index]
                ProductCard(index, product, basketOfProducts, opened_product)
            }
        }
        Spacer(modifier = Modifier.height(280.dp))
    }
    else
    {
        val scrollState = rememberScrollState()

        Column(modifier = Modifier.padding(20.dp, 15.dp).verticalScroll(scrollState))
        {
            Button(
                onClick = { opened_product.value = -1 },
                modifier = Modifier.padding(5.dp, 5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                shape = RoundedCornerShape(3.dp)

            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Обратно")
            }

            DisplayImageFromUrl(
                products[opened_product.value].image_url,
                modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp).size(350.dp).padding(20.dp, 20.dp)
            )

            Text(modifier = Modifier.padding(20.dp, 10.dp, 20.dp, 5.dp),
                text = products[opened_product.value].name, fontFamily = MainFont, fontSize = 45.sp)

            Text(
                modifier = Modifier.padding(20.dp, 0.dp, 20.dp, 5.dp),
                text = "${products[opened_product.value].price} ₽",
                fontFamily = MainFont,
                fontSize = 25.sp
            )

            Button(
                onClick = {

                    val existingProduct = basketOfProducts.find { it.name == products[opened_product.value].name }

                    if (existingProduct != null) {
                        existingProduct.count += 1
                    } else {
                        basketOfProducts.add(products[opened_product.value])
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(20.dp, 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                shape = RoundedCornerShape(3.dp)

            ) {
                Text(text = "В корзину",
                    modifier = Modifier.padding(0.dp),
                    fontSize = 18.sp,
                    fontFamily = MainFont,
                    color = Color(255, 255, 255))
            }

            Text(
                modifier = Modifier.padding(20.dp, 0.dp, 20.dp, 5.dp),
                text = products[opened_product.value].description,
                fontFamily = MainFont,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(190.dp))

        }
    }
}

@Composable

fun Profile(isitFirstLaunch : MutableState<Boolean>)
{
    val scrollState3 = rememberScrollState()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp).verticalScroll(scrollState3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var opened_menu_profile = remember { mutableStateOf(-1) }

        if (opened_menu_profile.value == -1)
        {
            // Иконка пользователя
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User Icon",
                tint = PurpleColor,
                modifier = Modifier
                    .size(220.dp)
                    .padding(top = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(32.dp))

            // Список возможностей
            val options = listOf(
                "Панель редактирования",
                "Мои заказы",
                "Настройки",
                "Поддержка",
                "Выход"
            )

            options.forEachIndexed { index, option ->
                ProfileOption(index, option, opened_menu_profile)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        else
        {
            Button(
                onClick = { opened_menu_profile.value = -1 },
                modifier = Modifier.padding(15.dp, 15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                shape = RoundedCornerShape(3.dp)

            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
            }

            if (opened_menu_profile.value == 0)
            {
                AddProductScreen()
            }
            else if (opened_menu_profile.value == 1)
            {

            }
            else if (opened_menu_profile.value == 2)
            {
                Button(
                    onClick =
                    {
                        isitFirstLaunch.value = true
                        setFirstLaunchNotCompleted(context = context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Выход из аккаунта",
                            modifier = Modifier.padding(0.dp),
                            fontSize = 18.sp,
                            fontFamily = MainFont,
                            color = Color(255, 255, 255)
                        )
                    }
                }
            }
            else if (opened_menu_profile.value == 3)
            {

            }
            else if (opened_menu_profile.value == 4)
            {
                val context = LocalContext.current
                (context as? Activity)?.finish()
            }

            Spacer(modifier = Modifier.height(150.dp))

        }
    }
    //AddProductScreen()
}

@Composable
fun ProfileOption(Index : Int, title: String, opened_menu_profile: MutableState<Int>) {

    Button(
        onClick = { opened_menu_profile.value = Index },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
        shape = RoundedCornerShape(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(0.dp),
                fontSize = 18.sp,
                fontFamily = MainFont,
                color = Color(255, 255, 255)
            )
        }
    }

}

@Composable
fun ProductCard(Index : Int, product: Product, basketOfProducts: MutableList<Product>, openedProduct: MutableState<Int>) {
    Card(
        shape = RectangleShape,
        modifier = Modifier
            .height(280.dp)
            .fillMaxWidth()
            .clickable {
                openedProduct.value = Index;
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),

    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
        ) {
            // Например, картинка товара (если есть)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                DisplayImageFromUrl(
                    product.image_url,
                    modifier = Modifier.padding(horizontal = 0.dp, vertical =0.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name,
                fontFamily = MainFont,
                fontSize = 18.sp,
                maxLines = 1, // Ограничиваем текст одной строкой
                overflow = TextOverflow.Ellipsis, // Добавляем многоточие в конце
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = "${product.price} ₽",
                fontFamily = MainFont,
                fontSize = 12.sp,
                maxLines = 1, // Ограничиваем текст одной строкой
                overflow = TextOverflow.Ellipsis, // Добавляем многоточие в конце
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))


            Button(
                onClick =
                {
                    val existingProduct = basketOfProducts.find { it.name == product.name }

                    if (existingProduct != null) {
                        existingProduct.count += 1
                    } else {
                        basketOfProducts.add(product)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                shape = RoundedCornerShape(3.dp)

            ) {
                Text(text = "В корзину",
                    modifier = Modifier.padding(0.dp),
                    fontSize = 18.sp,
                    fontFamily = MainFont,
                    color = Color(255, 255, 255))
            }
        }
    }
}

@Composable
fun CategoryCard(Index : Int, product: Product, openedProduct: MutableState<Int>, color__ : Color, Update: MutableState<Boolean>) {
    Card(
        shape = RectangleShape,
        modifier = Modifier
            .height(130.dp)
            .fillMaxWidth()
            .clickable {
                Update.value = true;
                openedProduct.value = Index;
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = color__.copy(alpha = 0.5f)),

        ) {
        Column(
            modifier = Modifier
                .padding(0.dp)
        ) {
            // Например, картинка товара (если есть)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(color__.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                DisplayImageFromUrl(
                    product.image_url,
                    modifier = Modifier.padding(horizontal = 0.dp, vertical =0.dp), alpha = 0.2f
                )

                Text(
                    text = product.name,
                    modifier = Modifier.padding(0.dp),
                    fontSize = 25.sp,
                    fontFamily = MainFont,
                    color = Color(15, 15, 15, 180)
                )
            }

        }
    }
}

@Composable
fun AddProductScreen() {
    // Состояния для каждого поля
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }
    var Id by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    // Состояние для показа ошибок
    var errorMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(0.dp)) {
        // Название товара

        Text(text = "Имя товара", fontFamily = MainFont)
        TextField(
            leadingIcon = { Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = null) },
            modifier = Modifier
                .padding(15.dp, 5.dp)
                .size(370.dp, 50.dp)
            ,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                focusedLeadingIconColor = Color.Black,
                unfocusedLeadingIconColor = Color.Black,
            ),
            shape = RectangleShape, // Убираем скругление
            value = name,
            onValueChange = { name = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Айди", fontFamily = MainFont)
        TextField(
            leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null) },
            modifier = Modifier
                .padding(15.dp, 5.dp)
                .size(370.dp, 50.dp)
            ,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                focusedLeadingIconColor = Color.Black,
                unfocusedLeadingIconColor = Color.Black,
            ),
            shape = RectangleShape, // Убираем скругление
            value = Id,
            onValueChange = { Id = it }
        )


        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Цена", fontFamily = MainFont)
        TextField(
            leadingIcon = { Icon(Icons.Filled.AttachMoney, contentDescription = null) },
            modifier = Modifier
                .padding(15.dp, 5.dp)
                .size(370.dp, 50.dp)
            ,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                focusedLeadingIconColor = Color.Black,
                unfocusedLeadingIconColor = Color.Black,
            ),
            shape = RectangleShape, // Убираем скругление
            value = price,
            onValueChange = { price = it }
        )


        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Описание", fontFamily = MainFont)
        // Описание товара
        TextField(
            leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
            modifier = Modifier
                .padding(15.dp, 5.dp)
                .size(370.dp, 50.dp)
            ,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                focusedLeadingIconColor = Color.Black,
                unfocusedLeadingIconColor = Color.Black,
            ),
            shape = RectangleShape, // Убираем скругление
            value = description,
            onValueChange = { description = it }
        )


        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Категория", fontFamily = MainFont)
        // Категория товара
        TextField(
            leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null) },
            modifier = Modifier
                .padding(15.dp, 5.dp)
                .size(370.dp, 50.dp)
            ,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                focusedLeadingIconColor = Color.Black,
                unfocusedLeadingIconColor = Color.Black,
            ),
            shape = RectangleShape, // Убираем скругление
            value = categoryId,
            onValueChange = { categoryId = it }
        )


        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Изображение", fontFamily = MainFont)
        // URL изображения
        TextField(
            leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
            modifier = Modifier
                .padding(15.dp, 5.dp)
                .size(370.dp, 50.dp)
            ,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                focusedLeadingIconColor = Color.Black,
                unfocusedLeadingIconColor = Color.Black,
            ),
            shape = RectangleShape, // Убираем скругление
            value = imageUrl,
            onValueChange = { imageUrl = it }
        )


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && price.isNotEmpty() && categoryId.isNotEmpty() && imageUrl.isNotEmpty()) {
                    // Отправляем запрос на сервер для создания нового товара
                    val product = Product(
                        name = name,
                        id = Id.toInt(),
                        price = price.toDouble(),
                        description = description,
                        categoryId = categoryId.toInt(),
                        image_url = imageUrl
                    )
                    // Вызов функции для отправки данных на сервер
                    sendProductToServer(product)
                } else {
                    errorMessage = "Все поля должны быть заполнены!"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
            shape = RoundedCornerShape(3.dp)

        ) {
            Text(text = "Добавить в базу данных",
                modifier = Modifier.padding(0.dp),
                fontSize = 18.sp,
                fontFamily = MainFont,
                color = Color(255, 255, 255))
        }

        // Отображение ошибки, если поля не заполнены
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
@Composable
fun Searching(products: List<Product>, basket: MutableList<Product>, opened_product: MutableState<Int>)
{
    var searchResults = remember { mutableStateListOf<Product>() }
    var text by remember { mutableStateOf("") }
    var update = remember { mutableStateOf(true) }

    var opened_category = remember { mutableStateOf(-1) }
    var last_opened_category = remember { mutableStateOf(-1) }

    if (opened_product.value == -1)
    {
        TextField(
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier
                .padding(15.dp, 15.dp)
                .size(370.dp, 50.dp)
            ,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                focusedLeadingIconColor = Color.Black,
                unfocusedLeadingIconColor = Color.Black,
            ),
            shape = RectangleShape, // Убираем скругление
            value = text,
            onValueChange = { text = it; searchResults.clear(); update.value = true }
        )
    }

    var category_cards = mutableListOf<Product>()

    category_cards.add(Product(name = "Телефоны", price = 0.0, image_url = "https://rms.kufar.by/v1/list_thumbs_2x/adim1/60699b8d-8104-408a-a1e8-7f7bd6c7a360.jpg"))
    category_cards.add(Product(name ="Компьютеры",price = 0.0,image_url =  "https://www.aq.ru/upload/imgresize/products/85818/detail/650x480/4-85-19200.jpg"))
    category_cards.add(Product(name ="Телевизоры",price = 0.0, image_url = "https://ae01.alicdn.com/kf/Ace31d14db45c4873a0211cd4f463e7075.jpg"))
    category_cards.add(Product(name ="Наушники",price = 0.0, image_url = "https://hi-fi.by/wp-content/uploads/2021/12/naushniki-audio-technica-ath-m50x.jpg"))
    category_cards.add(Product(name ="Пылесосы",price = 0.0,image_url = "https://karchershop.by/wp-content/uploads/2024/05/1198053_hero_02-Web_1200_Max.jpg"))
    category_cards.add(Product(name ="Мыши", price =0.0, image_url = "https://minio.nplus1.ru/app-images/659399/630366235d38f_img_desktop.jpg"))
    category_cards.add(Product(name ="Клавиатуры",price = 0.0,image_url =  "https://ir-3.ozone.ru/s3/multimedia-r/c1000/6622810695.jpg"))
    category_cards.add(Product(name ="Медикаменты", price =0.0, image_url = "https://upload.wikimedia.org/wikipedia/commons/0/03/Medikamente.jpg"))

    var show_all_results : Boolean = false

    if (opened_category.value == -1)
    {
        if (text.length > 0)
            show_all_results = true

        if (!show_all_results) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 элемента в линии
                modifier = Modifier.padding(0.dp, 100.dp, 0.dp, 90.dp).fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(category_cards.size) { index ->
                    val category = category_cards[index]
                    var color_ : Color = Color(0, 0, 0)

                    if (index % 2 == 0)
                        color_ = Color(218, 216, 220, 255)
                    if (index % 2 == 1)
                        color_ = Color(185, 185, 185, 255)

                    CategoryCard(index, category, opened_category, color_, update)
                }
            }
            Spacer(modifier = Modifier.height(250.dp))
        }
    }

    if (last_opened_category.value != opened_category.value)
    {
        last_opened_category.value = opened_category.value;
        update.value = true;
    }

    if (update.value) {
        searchResults.clear()
        for (product in products)
        {
            if (product.name.contains(text.toString(), ignoreCase = true))
            {
                if (opened_category.value != -1)
                {
                    if (product.categoryId == opened_category.value)
                    {
                        searchResults.add(product)
                    }
                }
                else {
                    searchResults.add(product)
                }
            }
        }
        update.value = false;
    }

    if (opened_category.value != -1 || show_all_results)
    {
        if (opened_product.value == -1 && !show_all_results) {
            Button(
                onClick = { opened_category.value = -1 },
                modifier = Modifier.padding(15.dp, 90.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                shape = RoundedCornerShape(3.dp)

            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
            }
        }

        val scrollState = rememberScrollState()

        if (opened_product.value == -1) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 элемента в линии
                modifier = Modifier.padding(0.dp, 150.dp, 0.dp, 90.dp).fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults.size) { index ->
                    val product = searchResults[index]
                    ProductCard(index, product, basket, opened_product)
                }
            }
        }
        else {

            val scrollState3 = rememberScrollState()

            Column(modifier = Modifier.padding(20.dp, 15.dp).verticalScroll(scrollState3))
            {
                Button(
                    onClick = { opened_product.value = -1 },
                    modifier = Modifier.padding(5.dp, 5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                    shape = RoundedCornerShape(3.dp)

                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                }

                DisplayImageFromUrl(
                    searchResults[opened_product.value].image_url,
                    modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp).size(350.dp)
                        .padding(20.dp, 20.dp)
                )

                Text(
                    modifier = Modifier.padding(20.dp, 10.dp, 20.dp, 5.dp),
                    text = searchResults[opened_product.value].name,
                    fontFamily = MainFont,
                    fontSize = 45.sp
                )

                Text(
                    modifier = Modifier.padding(20.dp, 0.dp, 20.dp, 5.dp),
                    text = "${searchResults[opened_product.value].price} ₽",
                    fontFamily = MainFont,
                    fontSize = 25.sp
                )

                Button(
                    onClick =
                    {
                        val existingProduct = basket.find { it.name == searchResults[opened_product.value].name }

                        if (existingProduct != null) {
                            existingProduct.count += 1
                        } else {
                            basket.add(searchResults[opened_product.value])
                        }
                    },

                    modifier = Modifier.fillMaxWidth().padding(20.dp, 20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                    shape = RoundedCornerShape(3.dp)

                ) {
                    Text(
                        text = "В корзину",
                        modifier = Modifier.padding(0.dp),
                        fontSize = 18.sp,
                        fontFamily = MainFont,
                        color = Color(255, 255, 255)
                    )
                }

                Text(
                    modifier = Modifier.padding(20.dp, 0.dp, 20.dp, 5.dp),
                    text = searchResults[opened_product.value].description,
                    fontFamily = MainFont,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(190.dp))
            }
        }
    }


}

@Composable
fun Basket(basket: MutableList<Product>)
{
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (basket.size < 1) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBasket,
                    contentDescription = "smile",
                    tint = PurpleColor,
                    modifier = Modifier
                        .size(150.dp)
                        .padding(5.dp)
                )
                Text(
                    "Корзина пуста :(",
                    fontSize = 25.sp,
                    fontFamily = MainFont,
                    color = Color(0, 0, 0)
                )
            }
        }
    }


    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        items(basket) { product ->

            OutlinedCard (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = Color(255, 255, 255, 10)
                ),
                border = BorderStroke(2.dp, color = PurpleColor)

            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(15.dp))
                {
                    Text(
                        text = product.name,
                        fontSize = 30.sp,
                        fontFamily = MainFont,
                        color = Color(0, 0, 0)
                    )

                }

                var product_count_text = remember { mutableStateOf("") }

                product_count_text.value = " В количестве : " + product.count.toString()

                Box(modifier = Modifier.fillMaxWidth())
                {
                    Text(
                        text = product.price.toString() + "₽",
                        fontSize = 19.sp,
                        modifier = Modifier.align(Alignment.CenterStart).padding(18.dp, 15.dp),
                        fontFamily = MainFont,
                        color = PurpleColor,
                    )
                }

                Text(
                    modifier = Modifier.padding(15.dp, 0.dp),
                    text = product_count_text.value.toString(),
                    fontSize = 21.sp,
                    fontFamily = MainFont,
                    color = Color(0, 0, 0)
                )

                Button(
                    onClick =
                    {
                        val existingProduct = basket.find { it.name == product.name }

                        if (existingProduct != null)
                        {
                            if (product.count == 1)
                            {
                                basket.remove(product)
                            }
                            else
                            {
                                product.count--
                            }

                        } else {
                            basket.remove(product)
                        }

                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 15.dp),
                    shape = RoundedCornerShape(3.dp)
                )

                {
                    Text(
                        "Убрать с корзины",
                        modifier = Modifier.padding(5.dp),
                        fontSize = 18.sp,
                        fontFamily = MainFont,
                        color = Color(255, 255, 255)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(250.dp))
}

fun isFirstLaunch(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("is_first_launch", true) // Возвращает true, если первый запуск
}


fun setFirstLaunchCompleted(context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    sharedPreferences.edit().putBoolean("is_first_launch", false).apply() // Устанавливает, что первый запуск завершён
}

fun setFirstLaunchNotCompleted(context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    sharedPreferences.edit().putBoolean("is_first_launch", true).apply() // Устанавливает, что первый запуск завершён
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateUI() {

    var products = remember { mutableStateOf<List<Product>>(emptyList()) }

    var basket_of_products = remember { mutableStateListOf<Product>() }

    var scope = rememberCoroutineScope()

    var selectedItem = remember { mutableStateOf(0) }

    var update_class = remember { mutableStateOf(false) }

    var firstLaunch by remember { mutableStateOf(true) }

    var opened_product = remember { mutableStateOf(-1) }

    var opened_product_search = remember { mutableStateOf(-1) }

    var login_or_reg by remember { mutableStateOf(0) }

    var isItMyFirstLaunch = remember { mutableStateOf(false) }

    if (firstLaunch)
    {
        isItMyFirstLaunch.value = isFirstLaunch(LocalContext.current)
        firstLaunch = false
    }

    if (isItMyFirstLaunch.value)
    {
        Column(modifier = Modifier.fillMaxSize())
        {
            Spacer(modifier = Modifier.height(150.dp))

            if (login_or_reg == 0) {

                Box(modifier = Modifier.fillMaxWidth())
                {
                    Column(modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally)
                    {
                        // Иконка пользователя
                        Icon(
                            imageVector = Icons.Default.HowToReg,
                            contentDescription = "User Icon",
                            tint = PurpleColor,
                            modifier = Modifier
                                .size(220.dp)
                                .padding(top = 32.dp)
                        )

                        Text(text = "Добро пожаловать!",
                            modifier = Modifier.padding(25.dp, 0.dp),
                            fontSize = 45.sp,
                            fontFamily = MainFont)

                        Spacer(modifier = Modifier.height(15.dp))

                        Text(text = "Скажи скидкам Гой-ДА!",
                            modifier = Modifier,
                            fontSize = 21.sp,
                            fontFamily = MainFont)

                        Spacer(modifier = Modifier.height(65.dp))


                        Button(
                            onClick = { login_or_reg = 1 },
                            modifier = Modifier.width(300.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                            shape = RoundedCornerShape(3.dp)
                        ) {

                            Text(
                                text = "Вход",
                                modifier = Modifier.padding(0.dp),
                                fontSize = 18.sp,
                                fontFamily = MainFont,
                                color = Color(255, 255, 255)
                            )

                        }
                        Button(
                            onClick = { login_or_reg = 2 },
                            modifier = Modifier.width(300.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                            shape = RoundedCornerShape(3.dp)
                        ) {

                            Text(
                                text = "Регистрация",
                                modifier = Modifier.padding(0.dp),
                                fontSize = 18.sp,
                                fontFamily = MainFont,
                                color = Color(255, 255, 255)
                            )

                        }
                    }
                }
            } else {
                Button(
                    onClick = { login_or_reg = 0 },
                    modifier = Modifier.padding(15.dp, 5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleColor),
                    shape = RoundedCornerShape(3.dp)

                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Обратно")
                }

                if (login_or_reg == 1) {
                    LoginScreen(isItMyFirstLaunch)
                } else {
                    RegistrationScreen(isItMyFirstLaunch)
                }

            }




            return
        }
    }

    LaunchedEffect(Unit) {
        if (update_class.value == false)
        {
            products.value = fetchProducts()
            update_class.value = true;
        }
    }

    Column(
        Modifier
            .background(BackgroundColor)
            .fillMaxSize()
            .padding(0.dp)
    ) {

        Box(modifier = Modifier.size(500.dp, 90.dp).background(PurpleColor).padding(18.dp, 40.dp, 0.dp, 0.dp))
        {
            Text(text = "GoydaMarket", fontFamily = MainFont, fontSize = 25.sp, color = TextColor   )
        }

        Column(
            Modifier
                .background(BackgroundColor)
                .fillMaxSize()
                .padding(15.dp)
        ) {

        Box(modifier = Modifier.fillMaxSize()) {

            var IsPageRefreshing by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            val refrstate = rememberPullToRefreshState()

            PullToRefreshBox(isRefreshing = IsPageRefreshing, state = refrstate, onRefresh = {
                IsPageRefreshing = true
                coroutineScope.launch {
                    delay(100)
                    products.value = fetchProducts()
                    IsPageRefreshing = false
                }
            },
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = IsPageRefreshing,
                        containerColor = PurpleColor,
                        color = Color(255, 255, 255),
                        state = refrstate
                    )
                },

                )

            {
                if (selectedItem.value == 0)
                {
                    ProductList(products.value, basket_of_products, opened_product)
                }
                else if (selectedItem.value == 1)
                {
                    Searching(products.value, basket_of_products, opened_product_search)
                }
                else if (selectedItem.value == 2)
                {
                    Basket(basket_of_products)
                }
                else if (selectedItem.value == 3)
                {
                   Profile(isItMyFirstLaunch)
                }
            }

            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter).absoluteOffset(0.dp, 15.dp),
                containerColor = BackgroundColor,
                contentColor = Color.Black
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Домой") },
                    label = { Text("Домой", fontFamily = MainFont) },
                    selected = selectedItem.value == 0,
                    onClick = { selectedItem.value = 0 }

                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
                    label = { Text("Поиск", fontFamily = MainFont) },
                    selected = selectedItem.value == 1,
                    onClick = { selectedItem.value = 1 }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Корзина") },
                    label = { Text("Корзина", fontFamily = MainFont) },
                    selected = selectedItem.value == 2,
                    onClick = { selectedItem.value = 2 }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.People, contentDescription = "Профиль") },
                    label = { Text("Профиль", fontFamily = MainFont) },
                    selected = selectedItem.value == 3,
                    onClick = { selectedItem.value = 3 }
                )
            }
            }
        }





    }
}

suspend fun fetchProducts(): List<Product> {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://93.125.42.181:3001/products")
                .build()
            val response = client.newCall(request).execute()
            val jsonData = response.body?.string()

            if (!response.isSuccessful || jsonData == null) {
                emptyList() // Возвращаем пустой список при ошибке
            } else {
                parseProducts(jsonData)
            }

        } catch (e: Exception) {
            emptyList() // Возвращаем пустой список при любой ошибке
        }
    }
}

fun parseProducts(jsonData: String?): List<Product> {
    val productList = mutableListOf<Product>()
    if (jsonData == null) return productList

    try {
        val jsonArray = JSONArray(jsonData)
        for (i in 0 until jsonArray.length()) {
            try {
                val element = jsonArray.get(i)
                if (element is JSONObject) {
                    val id = element.optInt("id", -1)
                    val name = element.optString("name", "Без названия")
                    val price = element.optDouble("price", 0.0)
                    val imageUrl = element.optString("path", "")
                    val categoryId = element.optInt("categoryId", 0)
                    val description = element.optString("description", "")

                    // Добавляем только те элементы, у которых есть корректный id (например, id != -1)
                    if (id != -1) {
                        productList.add(
                            Product(
                                id = id,
                                name = name,
                                price = price,
                                image_url = imageUrl,
                                categoryId = categoryId,
                                description = description
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace() // В режиме разработки можно выводить ошибку, в продакшене стоит убрать
                // Пропускаем некорректный элемент
            }
        }
    } catch (e: Exception) {
        e.printStackTrace() // Ошибка при создании JSONArray
    }
    return productList
}