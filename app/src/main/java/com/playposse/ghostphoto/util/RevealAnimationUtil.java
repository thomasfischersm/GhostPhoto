package com.playposse.ghostphoto.util;

import android.animation.Animator;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * A utility that starts reveal and hide animations for panels.
 */
public class RevealAnimationUtil {

    private RevealAnimationUtil() {}

    public static void startRevealAnimation(View originView, View layoutView) {
        startRevealAnimation(originView, layoutView, true);
    }

    public static void startRevealAnimation(View originView, View layoutView, boolean hideOrigin) {
        int centerX = (originView.getLeft() + originView.getRight()) / 2;
        int centerY = (originView.getTop() + originView.getBottom()) / 2;
        int startRadius = 0;
        int endRadius = Math.max(layoutView.getWidth(), layoutView.getHeight());

        Animator animator = ViewAnimationUtils.createCircularReveal(
                layoutView,
                centerX,
                centerY,
                startRadius,
                endRadius);

        layoutView.setVisibility(View.VISIBLE);
        if (hideOrigin) {
            originView.setVisibility(View.GONE);
        }
        animator.start();    }

    public static void startHideAnimation(View originView, View layoutView) {
        startHideAnimation(originView, layoutView, true);
    }

    public static void startHideAnimation(
            final View originView,
            final View layoutView,
            final boolean revealOrigin) {

        int centerX = (originView.getLeft() + originView.getRight()) / 2;
        int centerY = (originView.getTop() + originView.getBottom()) / 2;
        int startRadius = Math.max(layoutView.getWidth(), layoutView.getHeight());
        int endRadius = 0;

        Animator animator = ViewAnimationUtils.createCircularReveal(
                layoutView,
                centerX,
                centerY,
                startRadius,
                endRadius);

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Nothing to do.
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                layoutView.setVisibility(View.GONE);

                if (revealOrigin) {
                    originView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Nothing to do.
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Nothing to do.
            }
        });

        animator.start();
    }
}
