package me.srikavin.quiz.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import me.srikavin.quiz.R

enum class GameModes {
    OFFLINE,
    ONLINE_1_V_1
}

typealias GameModeSelectListener = (GameModes) -> Unit

class GameModeSelectDialog(context: Context, val onSelect: GameModeSelectListener, onClose: () -> Unit) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.game_mode_select_dialog)

        findViewById<LinearLayout>(R.id.game_mode_select_offline).setOnClickListener {
            onSelect(GameModes.OFFLINE)
            dismiss()
        }
        findViewById<LinearLayout>(R.id.game_mode_select_online).setOnClickListener {
            onSelect(GameModes.ONLINE_1_V_1)
            dismiss()
        }
    }
}