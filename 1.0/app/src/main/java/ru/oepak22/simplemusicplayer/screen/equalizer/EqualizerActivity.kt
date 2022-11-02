package ru.oepak22.simplemusicplayer.screen.equalizer

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import ru.oepak22.simplemusicplayer.MainApp
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.content.Defines
import ru.oepak22.simplemusicplayer.content.EqualizerSettings
import ru.oepak22.simplemusicplayer.data.DataOperations
import ru.oepak22.simplemusicplayer.media.MusicPlayerService
import ru.oepak22.simplemusicplayer.widget.AnalogController
import javax.inject.Inject

// класс активности для настроек эквалайзера
class EqualizerActivity : AppCompatActivity() {

    @Inject lateinit var mService: DataOperations

    // настройки эквалайзера
    private var mSettings: EqualizerSettings? = null
    private lateinit var mEqualizer: Equalizer

    // кнопки эквалайзера
    private lateinit var resetButton: ImageView
    private lateinit var equalizerSwitch: SwitchCompat

    // спиннер предустановок эквалайзера
    private lateinit var presetSpinner: Spinner
    private lateinit var spinnerDropDownIcon: ImageView

    // набор частотных полос
    private var seekBarFinal = arrayOfNulls<SeekBar>(5)
    private lateinit var points: FloatArray

    // тумблеры
    private lateinit var bassController: AnalogController
    private lateinit var reverbController: AnalogController

    // регуляторы и подписи к ним
    private lateinit var seekBar1: SeekBar
    private lateinit var textView1: TextView
    private lateinit var seekBar2: SeekBar
    private lateinit var textView2: TextView
    private lateinit var seekBar3: SeekBar
    private lateinit var textView3: TextView
    private lateinit var seekBar4: SeekBar
    private lateinit var textView4: TextView
    private lateinit var seekBar5: SeekBar
    private lateinit var textView5: TextView

    // диалоговое сообщение
    private var mDialog: AlertDialog? = null


    // создание активности
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)

        MainApp.sAppComponent.injectEqualizerActivity(this)

        // инициализация компонентов активности
        resetButton = findViewById(R.id.equalizer_reset_btn)
        equalizerSwitch = findViewById(R.id.equalizer_switch)
        presetSpinner = findViewById(R.id.equalizer_preset_spinner)
        spinnerDropDownIcon = findViewById(R.id.spinner_dropdown_icon)
        seekBar1 = findViewById(R.id.seekBar1)
        textView1 = findViewById(R.id.textView1)
        seekBar2 = findViewById(R.id.seekBar2)
        textView2 = findViewById(R.id.textView2)
        seekBar3 = findViewById(R.id.seekBar3)
        textView3 = findViewById(R.id.textView3)
        seekBar4 = findViewById(R.id.seekBar4)
        textView4 = findViewById(R.id.textView4)
        seekBar5 = findViewById(R.id.seekBar5)
        textView5 = findViewById(R.id.textView5)
        bassController = findViewById(R.id.controllerBass)
        reverbController = findViewById(R.id.controller3D)

        // инициализация эквалайзера
        mEqualizer = Equalizer(0, 0)
        loadSettings()
    }

    // приостановка работы активности
    override fun onPause() {
        super.onPause()
        saveSettings()
    }

    // уничтожение активности
    override fun onDestroy() {
        super.onDestroy()

        mDialog?.dismiss()

        mEqualizer.release()
        resetButton.setOnClickListener(null)
        equalizerSwitch.setOnClickListener(null)
        spinnerDropDownIcon.setOnClickListener(null)
        presetSpinner.onItemSelectedListener = null

        seekBar1.setOnSeekBarChangeListener(null)
        seekBar2.setOnSeekBarChangeListener(null)
        seekBar3.setOnSeekBarChangeListener(null)
        seekBar4.setOnSeekBarChangeListener(null)
        seekBar5.setOnSeekBarChangeListener(null)

        bassController.setOnProgressChangedListener(null)
        reverbController.setOnProgressChangedListener(null)
    }

    // инициализация переключателей
    private fun initButtons() {

        // кнопка "Сброс"
        resetButton.setOnClickListener {
            resetSettings()
        }

        // кнопка включения эквалайзера
        equalizerSwitch.isChecked = mSettings!!.isEqualizerEnabled
        equalizerSwitch.setOnCheckedChangeListener { _, isChecked ->
            mSettings!!.isEqualizerEnabled = isChecked
            applySettings()
        }
    }

    // инициализация предустановок
    private fun initPreset() {

        // спиннер предустановок эквалайзера
        spinnerDropDownIcon.setOnClickListener { presetSpinner.performClick() }

        // применение существующих настроек
        if (mSettings!!.presetPos == 0) {
            for (bandIdx in 0 until mEqualizer.numberOfBands)
                mEqualizer.setBandLevel(
                    bandIdx.toShort(),
                    mSettings!!.seekbarpos[bandIdx].toShort()
                )
        }
        else mEqualizer.usePreset((mSettings!!.presetPos - 1).toShort())

        // инициализация адаптера для спиннера
        val equalizerPresetNames = ArrayList<String>()
        val equalizerPresetSpinnerAdapter = ArrayAdapter(
            this,
            R.layout.recycler_presetpos,
            equalizerPresetNames
        )
        equalizerPresetSpinnerAdapter
            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        equalizerPresetNames.add("Custom")

        // заполнение списка предустановок эквалайзера
        for (i in 0 until mEqualizer.numberOfPresets)
            equalizerPresetNames.add(mEqualizer.getPresetName(i.toShort()))

        // установка адаптера в спиннер
        presetSpinner.adapter = equalizerPresetSpinnerAdapter
        if (mSettings!!.presetPos != 0)
            presetSpinner.setSelection(mSettings!!.presetPos)

        // установка обработчика спиннера
        presetSpinner.onItemSelectedListener = object : AdapterView.OnItemClickListener,
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                try {
                    if (p2 != 0) {
                        mEqualizer.usePreset((p2 - 1).toShort())
                        mSettings!!.presetPos = p2

                        val lowerEqualizerBandLevel = mEqualizer.bandLevelRange[0]

                        for (i in 0..4) {
                            seekBarFinal[i]!!.progress = mEqualizer.getBandLevel(i.toShort()) - lowerEqualizerBandLevel
                            points[i] = (mEqualizer.getBandLevel(i.toShort()) - lowerEqualizerBandLevel).toFloat()
                            mSettings!!.seekbarpos[i] = mEqualizer.getBandLevel(i.toShort()).toInt()
                        }
                        applySettings()
                    }
                }
                catch (e: Exception) {
                    Toast.makeText(this@EqualizerActivity, "Error while updating Equalizer", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {}
        }

    }

    // инициализация частотных полос
    @SuppressLint("SetTextI18n")
    private fun initBands() {

        points = FloatArray(5)

        // верхняя и нижняя частотные границы
        val lowerEqualizerBandLevel = mEqualizer.bandLevelRange[0]
        val upperEqualizerBandLevel = mEqualizer.bandLevelRange[1]

        // цикл по частотным полосам
        for (i in 0..4) {

            val equalizerBandIndex: Short = i.toShort()

            val frequencyHeaderTextView = TextView(this)
            frequencyHeaderTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            frequencyHeaderTextView.gravity = Gravity.CENTER_HORIZONTAL
            frequencyHeaderTextView.setTextColor(Color.parseColor("#FFFFFF"))
            frequencyHeaderTextView.text = (mEqualizer.getCenterFreq(equalizerBandIndex) / 1000)
                .toString() + "Hz"

            val seekBarRowLayout = LinearLayout(this)
            seekBarRowLayout.orientation = LinearLayout.VERTICAL

            val lowerEqualizerBandLevelTextView = TextView(this)
            lowerEqualizerBandLevelTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            lowerEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"))
            lowerEqualizerBandLevelTextView.text = (lowerEqualizerBandLevel / 100).toString() + "dB"

            val upperEqualizerBandLevelTextView = TextView(this)
            upperEqualizerBandLevelTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            upperEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"))
            upperEqualizerBandLevelTextView.text = (upperEqualizerBandLevel / 100).toString() + "dB"

            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.weight = 1F

            var seekBar = SeekBar(this)
            var textView = TextView(this)
            when (i) {
                0 -> {
                    seekBar = seekBar1
                    textView = textView1
                }
                1 -> {
                    seekBar = seekBar2
                    textView = textView2
                }
                2 -> {
                    seekBar = seekBar3
                    textView = textView3
                }
                3 -> {
                    seekBar = seekBar4
                    textView = textView4
                }
                4 -> {
                    seekBar = seekBar5
                    textView = textView5
                }
            }
            seekBarFinal[i] = seekBar
            seekBar.progressDrawable.colorFilter = PorterDuffColorFilter(
                Color.DKGRAY,
                PorterDuff.Mode.SRC_IN
            )
            seekBar.thumb.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            seekBar.id = i
            seekBar.max = upperEqualizerBandLevel - lowerEqualizerBandLevel

            textView.text = frequencyHeaderTextView.text
            textView.setTextColor(Color.WHITE)
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER

            points[i] = (mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel).toFloat()
            seekBar.progress = mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    mEqualizer.setBandLevel(equalizerBandIndex,
                        (p1 + lowerEqualizerBandLevel).toShort())
                    points[seekBar.id] = (mEqualizer.getBandLevel(equalizerBandIndex)
                            - lowerEqualizerBandLevel).toFloat()
                    mSettings!!.seekbarpos[seekBar.id] = p1 + lowerEqualizerBandLevel
                    applySettings()
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    presetSpinner.setSelection(0)
                    mSettings!!.presetPos = presetSpinner.selectedItemPosition
                    applySettings()
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
        }
    }

    // инициализация тумблеров
    private fun initThumbs() {

        // тумблер басов
        bassController.label = "BASS"
        bassController.circlePaint2.color = Color.parseColor("#B24242")
        bassController.linePaint.color = Color.parseColor("#B24242")
        bassController.invalidate()
        if (mSettings!!.bassStrength == 0) bassController.setProgress(1)
        else bassController.setProgress(mSettings!!.bassStrength)
        bassController.setOnProgressChangedListener(object : AnalogController.onProgressChangedListener {
            override fun onProgressChanged(progress: Int) {
                mSettings!!.bassStrength = progress
                applySettings()
            }
        })

        // тумблер реверберации
        reverbController.label = "3D"
        reverbController.circlePaint2.color = Color.parseColor("#B24242")
        reverbController.linePaint.color = Color.parseColor("#B24242")
        reverbController.invalidate()
        if (mSettings!!.reverbPreset == 0) reverbController.setProgress(1)
        else reverbController.setProgress(mSettings!!.reverbPreset)
        reverbController.setOnProgressChangedListener(object : AnalogController.onProgressChangedListener {
            override fun onProgressChanged(progress: Int) {
                mSettings!!.reverbPreset = progress
                applySettings()
            }
        })
    }

    // загрузка настроек эквалайзера
    private fun loadSettings() {
        mSettings = mService.restoreEqualizerSettings(this)
        if (mSettings == null) mSettings = EqualizerSettings()
        initButtons()
        initPreset()
        initBands()
        initThumbs()
    }

    // сохранение настроек эквалайзера
    private fun saveSettings() {
        mSettings?.let {
            mService.saveEqualizerSettings(this, it)
        }
    }

    // применение настроек эквалайзера
    private fun applySettings() {
        mSettings?.let {
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (MusicPlayerService::class.java.name == service.service.className) {
                    val intent = Intent(this, MusicPlayerService::class.java)
                    intent.putExtra("command", Defines.COMMAND_EQ)
                    intent.putExtra("eq", it)
                    startService(intent)
                }
            }
        }
    }

    // сброс настроек эквалайзера
    private fun resetSettings() {
        val builder = AlertDialog.Builder(this)
        with (builder) {
            setIcon(R.drawable.ic_message_warning)
            setTitle(R.string.message_warning)
            setMessage(R.string.message_sure_restart)
            setPositiveButton(R.string.message_ok) { _, _ ->
                mEqualizer = Equalizer(0, 0)
                mSettings = EqualizerSettings()
                initButtons()
                initPreset()
                initBands()
                initThumbs()
                applySettings()
            }
            setNegativeButton(R.string.message_cancel) { _, _ -> }
        }
        mDialog = builder.create()
        mDialog!!.show()
    }
}