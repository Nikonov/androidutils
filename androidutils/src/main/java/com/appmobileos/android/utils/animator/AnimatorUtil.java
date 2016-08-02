package com.appmobileos.android.utils.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

import java.util.List;

/**
 * Created by Andrey Nikonov on 02.04.16.
 */
public class AnimatorUtil {

    /**
     * Cancels animator if need
     *
     * @param animator animator which need to cancel
     */
    public static void cancelAnimatorIfNeed(Animator animator) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    public static ObjectAnimator alphaAnimatorAlpha(View view, boolean visible) {
        return alphaAnimatorAlpha(view, visible, 0, 0);
    }

    public static ObjectAnimator alphaAnimatorAlpha(View view, boolean visible, int duration, int startDelay) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, view.getAlpha(), visible ? 1.0f : 0.0f);
        animator.setDuration(duration);
        animator.setStartDelay(startDelay);
        return animator;
    }


    private static void createHintAnimation(final View hintView, boolean show, Animator.AnimatorListener listener) {
        ObjectAnimator animation = AnimatorUtil.alphaAnimatorAlpha(hintView, show, 400, 0);
        animation.addListener(listener);
        animation.start();
    }

    public static void showCommonHintAnimation(final View hintView) {
        createHintAnimation(hintView, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                hintView.setVisibility(View.VISIBLE);
            }
        });
    }

    public static void hideCommonHintAnimation(final View hintView, final View touchHintView) {
        createHintAnimation(hintView, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hintView.setVisibility(View.INVISIBLE);
                showTouchHintAnimation(touchHintView);
            }

        });
    }

    public static void showTouchHintAnimation(final View hintView) {
        createHintAnimation(hintView, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                hintView.setVisibility(View.VISIBLE);
            }
        });
    }

    public static void hideTouchHintAnimation(final View touchHintView, final AnimatorListenerAdapter callback) {
        createHintAnimation(touchHintView, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                touchHintView.setVisibility(View.INVISIBLE);
                callback.onAnimationEnd(animation);
            }

        });
    }
    public static void hideTouchHintAnimation(final View touchHintView) {
        createHintAnimation(touchHintView, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                touchHintView.setVisibility(View.INVISIBLE);
            }

        });
    }

    public static AnimatorListenerAdapter createShowHideVisible(final List<View> views, final boolean show) {
        return new AnimatorListenerAdapter() {
            private boolean isCanceled = false;

            @Override
            public void onAnimationStart(Animator animation) {
                if (show) {
                    for (View view : views) {
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show && !isCanceled) {
                    for (View view : views) {
                        view.setVisibility(View.GONE);
                    }
                }
                animation.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isCanceled = true;
            }
        };
    }
}
