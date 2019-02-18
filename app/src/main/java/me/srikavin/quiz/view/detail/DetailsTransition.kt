package me.srikavin.quiz.view.detail

import androidx.transition.ChangeBounds
import androidx.transition.ChangeClipBounds
import androidx.transition.ChangeTransform
import androidx.transition.TransitionSet

class DetailsTransition : TransitionSet() {
    init {
        ordering = TransitionSet.ORDERING_TOGETHER
        duration = 375
        addTransition(ChangeClipBounds()).addTransition(ChangeTransform()).addTransition(ChangeBounds())
    }
}