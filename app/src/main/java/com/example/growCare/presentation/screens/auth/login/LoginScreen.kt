package com.example.growCare.presentation.screens.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4)) // Hijau muda keputihan
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ilustrasi Header
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Sign In Illustration",
            modifier = Modifier
                .padding(top = 40.dp)
                .size(200.dp),
            tint = Color(0xFF4CAF50)
        )

        // Judul "Sign In"
        Text(
            text = "Sign In",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(top = 24.dp)
        )

        // Subtitle
        Text(
            text = "Enter valid user name & password to continue",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )

        // Input Field - Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("User name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedBorderColor = Color(0xFF4ADE80), // Hijau saat focused
                unfocusedBorderColor = Color(0xFFE5E7EB) // Abu-abu muda saat idle
            ),
            singleLine = true
        )

        // Input Field - Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else
                    Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedBorderColor = Color(0xFF4ADE80),
                unfocusedBorderColor = Color(0xFFE5E7EB)
            ),
            singleLine = true
        )

        // Forget Password Link
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Forget password",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2563EB),
                modifier = Modifier.clickable { /* TODO: Handle forget password */ }
            )
        }

        // Login Button
        Button(
            onClick = { /* TODO: Handle Login */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4ADE80), // Hijau cerah
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "Login",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Divider dengan Text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = Color(0xFFE5E7EB)
            )
            Text(
                text = "Or Continue with",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = Color(0xFFE5E7EB)
            )
        }

        // Social Login Buttons (Placeholder based on "Styling Tambahan")
        // Note: User didn't explicitly ask for buttons in the list, but implied by "Or Continue with" and styling.
        // I will leave them out to strictly follow the list, or add empty row if needed.
        // The prompt says "Divider dengan Text ... Margin top 24dp" then "Sign Up Link".
        // I will skip adding actual buttons to avoid cluttering if not explicitly requested in the component list.

        // Sign Up Link
        val signUpText = buildAnnotatedString {
            append("Haven't any account? ")
            withStyle(style = SpanStyle(color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold)) {
                pushStringAnnotation(tag = "SIGN_UP", annotation = "signup")
                append("Sign up")
                pop()
            }
        }

        ClickableText(
            text = signUpText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(top = 24.dp),
            onClick = { offset ->
                signUpText.getStringAnnotations(tag = "SIGN_UP", start = offset, end = offset).firstOrNull()?.let {
                    // Handle Sign Up click
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}

