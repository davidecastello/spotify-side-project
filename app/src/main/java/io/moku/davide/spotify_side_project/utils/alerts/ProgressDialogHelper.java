package io.moku.davide.spotify_side_project.utils.alerts;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import io.moku.davide.spotify_side_project.R;


/**
 * Created by Davide Castello on 16/01/18.
 * Project: ProgressDialogHelper
 * Copyright © 2018 Moku S.r.l. All rights reserved.
 *
 *
 * Requires:
 * 1. Lottie's library:
 *      - compile 'com.airbnb.android:lottie:2.3.1'
 * 2. These strings in your strings.xml
 *      - default_inital_msg                            Example: "Updating…"
 *      - default_done_msg                              Example: "Operation completed successfully!"
 *      - default_animation_name                        Example: "tick.json"
 * 3. The animation you've indicated as default
 *      - in JSON format
 *      - inside the 'assets' folder
 * 4. The layout 'progress_dialog_helper_layout.xml'
 */

public class ProgressDialogHelper {

    private static final String TAG = ProgressDialogHelper.class.getSimpleName();

    /* Fields */
    private Activity activity;
    private AlertDialog dialog;
    private TextView popupMsg;
    private ProgressBar progressBar;
    private LottieAnimationView lottie;
    private CountDownTimer timer;

    /* Customizable */
    private String initalMsg;
    private String doneMsg;
    private double finalDelaySeconds;
    private String animationName;
    private float speed;
    private OnDialogDismissedListener onDialogDismissedListener;

    /* Constants */
    private static final int SECONDS = 1000;
    private static final double DEFAULT_FINAL_DELAY_SECONDS = 0.5;
    private static final float DEFAULT_ANIMATION_SPEED = 1f;


    /* Constructor - You can also use the Builder */

    public ProgressDialogHelper(Activity activity, String animationName, float speed, String initalMsg, String doneMsg, double finalDelaySeconds) {
        this.activity = activity;
        this.animationName = animationName;
        this.speed = speed;
        this.initalMsg = initalMsg;
        this.doneMsg = doneMsg;
        this.finalDelaySeconds = finalDelaySeconds;
    }

    /* Builder pattern */

    public static class Builder {

        private Activity activity;
        private String animationName;
        private float speed;
        private String initalMsg;
        private String doneMsg;
        private double finalDelaySeconds;

        public Builder(Activity activity) {
            this.activity = activity;
            this.animationName = activity.getString(R.string.default_animation_name);
            this.speed = DEFAULT_ANIMATION_SPEED;
            this.initalMsg = activity.getString(R.string.default_inital_msg);
            this.doneMsg = activity.getString(R.string.default_done_msg);
            this.finalDelaySeconds = DEFAULT_FINAL_DELAY_SECONDS;
        }

        public Builder setActivity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public Builder setAnimationName(String animationName) {
            this.animationName = animationName;
            return this;
        }

        public Builder setSpeed(float speed) {
            this.speed = speed;
            return this;
        }

        public Builder setInitalMsg(String initalMsg) {
            this.initalMsg = initalMsg;
            return this;
        }

        public Builder setDoneMsg(String doneMsg) {
            this.doneMsg = doneMsg;
            return this;
        }

        public Builder setFinalDelaySeconds(double finalDelaySeconds) {
            this.finalDelaySeconds = finalDelaySeconds;
            return this;
        }

        public ProgressDialogHelper create() {
            return new ProgressDialogHelper(activity, animationName, speed, initalMsg, doneMsg, finalDelaySeconds);
        }

        public ProgressDialogHelper show() {
            return create().show();
        }
    }


    /* Setters */

    public void setInitalMsg(String initalMsg) {
        this.initalMsg = initalMsg;
    }

    public void setDoneMsg(String doneMsg) {
        this.doneMsg = doneMsg;
    }

    public void setFinalDelaySeconds(double finalDelaySeconds) {
        this.finalDelaySeconds = finalDelaySeconds;
    }

    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }


    /* OnDialogDismissedListener */

    public interface OnDialogDismissedListener {
        public void onDialogDismissed(Activity activity);
    }


    /* Public methods */

    public ProgressDialogHelper show() {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.progress_dialog_helper_layout, null);
        setupLottie(layout);
        progressBar = layout.findViewById(R.id.progressBar);
        popupMsg = layout.findViewById(R.id.popupMessage);
        popupMsg.setText(initalMsg);
        dialog = new AlertDialog.Builder(activity)
                .setView(layout)
                .setCancelable(false)
                .show();
        return this;
    }

    public void done() {
        done(null);
    }

    public void done(OnDialogDismissedListener onDialogDismissedListener) {
        this.onDialogDismissedListener = onDialogDismissedListener;
        popupMsg.setText(doneMsg);
        lottie.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        lottie.playAnimation();
    }

    public void dismiss() {
        dismissWithToast(null, 0);
    }

    public void dismissWithToast(String msg, int duration) {
        dismissDialog();
        if (msg != null) {
            Toast.makeText(activity.getApplicationContext(), msg, duration).show();
        }
    }


    /* Private methods */

    private void setupLottie(View view) {
        if (view != null) {
            lottie = view.findViewById(R.id.lottieAnimationView);
        } else {
            lottie = new LottieAnimationView(activity);
        }
        lottie.setAnimation(animationName);
        lottie.setSpeed(speed);
        lottie.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                long delay = (long) finalDelaySeconds * SECONDS;
                timer = new CountDownTimer(delay, 100) {
                    @Override
                    public void onTick(long l) {
                        // do nothing
                    }

                    @Override
                    public void onFinish() {
                        dismissDialog();
                        if (onDialogDismissedListener != null) {
                            onDialogDismissedListener.onDialogDismissed(activity);
                        }
                    }
                }.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void dismissDialog() {
        if(dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Error while dismissing the dialog!");
            } finally {
                dialog = null;
            }
        }
    }
}
