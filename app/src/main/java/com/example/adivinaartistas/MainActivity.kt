package com.example.adivinaartistas

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.get
import com.airbnb.lottie.LottieAnimationView
import com.github.javafaker.Faker
import com.google.android.flexbox.FlexboxLayout
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var txtPregunta: TextView
    private var respuesta: String = ""
    private lateinit var flexAlfabeto: FlexboxLayout
    private lateinit var flexResponse: FlexboxLayout
    private var indicesOcupados: ArrayList<Int> = arrayListOf()
    private var intentosPermitidos: Int = 0
    private var intentosHechos: Int = 0
    private lateinit var txtCantIntentos: TextView
    private lateinit var txtMsjIntentos: TextView
    private var finalizado: Boolean = false
    private lateinit var lottieResult: LottieAnimationView
    private lateinit var lotieAnimIngame: LottieAnimationView
    private lateinit var txtMsjResultado: TextView
    private lateinit var txtMsjRespuestaCorrecta: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //muestra el spash antes de que se le asigne a esta pantalla su recurso de diseño
        installSplashScreen()

        setContentView(R.layout.activity_main)

        //widgets
        txtPregunta = findViewById(R.id.txtPregunta)
        lotieAnimIngame = findViewById(R.id.animation_view_ingame)
        flexResponse = findViewById(R.id.edt)
        flexAlfabeto = findViewById(R.id.flexboxLayout)
        txtCantIntentos = findViewById(R.id.txtCantIntentos)
        txtMsjIntentos = findViewById(R.id.txtMsjIntentos)
        lottieResult = findViewById(R.id.animation_view_resultado)
        txtMsjResultado = findViewById(R.id.txtMsjResultado)
        txtMsjRespuestaCorrecta = findViewById(R.id.txtMsjRespuestaCorrecta)

        /*
        * Finalmente en el método onCreate debemos hacer el llamado a todas esas funciones que le dan el
        dinamismo a nuestra aplicación, tal como te muestro a continuacion.
        * */

        //1. generar palabra a adivinar
        //1.1 la cantidad de intetos permitidos se le dara: tamaño de caracteres + 2
        respuesta = obtenerPalabraAleatoria().uppercase()
        intentosPermitidos = respuesta.length + 2
        txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"

        //2. generar alfabeto que incluya las letras de la palabra a adivinar
        val alfabeto = generarAlfabeto(respuesta)

        //3. desordenar el alfabeto generado para que sea mas dinamica
        val alfabetoDesorden = desordenar(alfabeto)
        //4. generar los espacios donde se iran mostrando la respuesta
        mostrarEspacioRespuesta(respuesta.length, flexResponse)
        //4. mostrar en la vista cada letra generada como boton para que se pueda seleccionar
        mostarAlfabeto(alfabetoDesorden.uppercase(), flexAlfabeto)
    }

    //funciones

    fun generarAlfabeto(semilla: String): String {
        val randomValues = List(5) { Random.nextInt(65, 90).toChar() }
        return "$semilla${randomValues.joinToString(separator = "")}"
    }

    //Función que agarre la especie de alfabeto y lo desordene
    fun desordenar(theWord: String): String {

        val theTempWord = theWord.toMutableList()

        for (item in 0..Random.nextInt(1, theTempWord.count() - 1)) {
            val indexA = Random.nextInt(theTempWord.count() - 1)
            val indexB = Random.nextInt(theTempWord.count() - 1)
            val temp = theTempWord[indexA]
            theTempWord[indexA] = theTempWord[indexB]
            theTempWord[indexB] = temp
        }
        return theTempWord.joinToString(separator = "")
    }

    fun obtenerPalabraAleatoria(): String {
        val faker = Faker()
        val palabra = faker.leagueOfLegends().champion()

        return palabra.split(' ').get(0) //('').get(0) //aveces devuelve nombres compuestos
    }

    /*
    * Función para mostrar dinámicamente los espacios donde se iran mostrando cada una de las letras de
    la respuesta cuando aciertes y selecciones una letra correcta, usaremos FlexboxLayout para que se
    muestre en forma de filas y columnas
    * */

    fun mostrarEspacioRespuesta(cantidad: Int, vista: FlexboxLayout) {
        for (letter in 1..cantidad) {
            val btnLetra = EditText(this)
            btnLetra.isEnabled = false
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )

            layoutParams.setMargins(5, 5, 5, 5)
            btnLetra.layoutParams = layoutParams
            vista.addView(btnLetra)
        }
    }

    /*
    * Función para mostrar dinámicamente en la vista el alfabeto generado en el paso 1 y que se vea en
    forma de columnas y filas mediante FlexboxLayout. Le daremos un aspecto de botones para que se
    vea mas similar a un alfabeto seleccionable( es de gustos).
    * */

    fun mostarAlfabeto(alfabeto: String, vista: FlexboxLayout) {
        for (letter in alfabeto) {
            val btnLetra = Button(this)
            btnLetra.text = letter.toString()
            btnLetra.textSize = 12f
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(5, 5, 5, 5)
            btnLetra.layoutParams = layoutParams
            vista.addView((btnLetra))

            btnLetra.setOnClickListener {
                clickLetra(it as Button)
            }
        }
    }

    fun clickLetra(btnClicked: Button) {
        if (!finalizado) {
            //obtener el indice de la letra seleccionada inicialmente
            var starIndex = 0
            var resIndex = respuesta.indexOf(btnClicked.text.toString())
            //si el indice ya fue ocupado entonces no tomar en cuenta los indices hacia atras
            while (indicesOcupados.contains(resIndex)) {
                starIndex = resIndex + 1
                resIndex = respuesta.indexOf(btnClicked.text.toString(), starIndex)
            }
            //si la respuesta contiene la letra seleccionada
            if (resIndex != -1) {
                val flexRow = flexResponse.get(resIndex) as EditText
                flexRow.setText(respuesta.get(resIndex).toString())
                indicesOcupados.add(resIndex)
                btnClicked.setBackgroundColor(Color.GREEN)
                btnClicked.isEnabled = false
                btnClicked.setTextColor(Color.WHITE)
            } else {
                Toast.makeText(
                    applicationContext, "No es una letra valida",
                    Toast.LENGTH_SHORT).show()
                btnClicked.setBackgroundColor(Color.RED)
                btnClicked.isEnabled = false
                btnClicked.setTextColor(Color.WHITE)
            }
            intentosHechos++

            txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"
            verificarResultado()
        }
    }

    /*
    * Como veras de ultimo se muestra una llamada a la función verificar resultado, la cual, valida si se te
    terminaron los intentos o bien si ya adivinaste todas las letras de la respuesta correcta.
    * */
    fun verificarResultado() {

        if (intentosHechos == intentosPermitidos || indicesOcupados.size == respuesta.length) {
            finalizado = true

            //si gano o perdio
            if (indicesOcupados.size == respuesta.length) {
                lottieResult.setAnimation(R.raw.win)
                txtMsjResultado.text = "Felicidades!"
            } else {
                lottieResult.setAnimation(R.raw.lost)
                txtMsjResultado.text = "Perdiste :("
            }

            txtMsjRespuestaCorrecta.setText("La respuesta correcta es: $respuesta")

            //despues de configurar la vista ponerlas como visibles
            txtMsjResultado.visibility = View.VISIBLE
            lottieResult.visibility = View.VISIBLE
            txtMsjRespuestaCorrecta.visibility = View.VISIBLE

            //ocultar los que no se deben mostrar
            flexResponse.visibility = View.GONE
            txtCantIntentos.visibility = View.GONE
            flexAlfabeto.visibility = View.GONE
            txtMsjIntentos.visibility = View.GONE
            txtPregunta.visibility = View.GONE
            lotieAnimIngame.visibility = View.GONE
        }
    }

}