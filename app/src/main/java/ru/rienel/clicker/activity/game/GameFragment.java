package ru.rienel.clicker.activity.game;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import ru.rienel.clicker.R;
import ru.rienel.clicker.common.ImageDonut;
import ru.rienel.clicker.common.Preconditions;
import ru.rienel.clicker.ui.dialog.EndGameDialogFragment;
import ru.rienel.clicker.ui.dialog.ErrorMultiplayerDialogFragment;

import static ru.rienel.clicker.common.Configuration.SharedPreferencesKeys.PREFERENCES_DONUT_ID;
import static ru.rienel.clicker.common.Configuration.SharedPreferencesKeys.PREFERENCES_NAME;
import static ru.rienel.clicker.common.Configuration.SharedPreferencesKeys.PREFERENCES_VOLUME_EFFECT;
import static ru.rienel.clicker.common.Configuration.SharedPreferencesKeys.PREFERENCES_VOLUME_MUSIC;

public class GameFragment extends Fragment implements GameContract.View, SoundPool.OnLoadCompleteListener {
	private static final String TIMER_TIME_FORMAT = "%02d:%02d";
	private static final String TAG = GameFragment.class.getName();
	private static final String DIALOG_ERROR = ErrorMultiplayerDialogFragment.class.getName();

	private GameContract.Presenter presenter;

	private TextView coinsCounterTextView;
	private TextView multiplayerCoinsCounterTextView;
	private TextView newClick;
	private TextView clock;
	private ImageView donutImage;
	private TextView currentLevelTextView;
	private TextView time;
	private Button shop;
	private RoundCornerProgressBar progressBar;

	private Integer clicks;
	private int donutPerClick;
	private CountDownTimer countDownTimerBoost;
	private boolean flagShop;
	private long timer;
	private long currentTime;
	private int currentLevel;
	private int coins;
	private int multiplayerCoins;
	private int requiredClicks;
	private Animation rotateAnimation;
	private Animation timeAnimation;
	private Animation donutClickAnimation;
	private CountDownTimer countDownTimer;

	private Button incTap;
	private Button autoTap;
	private Button regularAutoTap;
	private Button regularIncTap;
	private int tempClicks;
	private int tempAutoClicks;
	private int mAutoClicks;

	private SoundPool clickSound;
	private int clickSoundId;
	private float volumeM, volumeE;
	private SoundPool backgroundSound;
	private int backgroundSoundId;
	private SharedPreferences saves, cookieSettings;

	private EndGameDialogFragment endGameDialogFragment;
	private ErrorMultiplayerDialogFragment errorDialogFragment;

	public static GameFragment newInstance() {
		return new GameFragment();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_game, container, false);

		saves = root.getContext().getSharedPreferences(getString(R.string.gameSaves), Context.MODE_PRIVATE);
		cookieSettings = root.getContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		loadGameSaves();
		startAutoClicks(this.mAutoClicks);
		incTap = root.findViewById(R.id.btnIncTap);
		incTap.setOnClickListener(newOnIncrementTapClickListener());
		autoTap = root.findViewById(R.id.btnAutoTap);
		autoTap.setOnClickListener(newOnAutoTapClickListener());
		regularAutoTap = root.findViewById(R.id.btnRegularAutoTap);
		regularAutoTap.setOnClickListener(newOnRegularAutoTapClickListener());
		regularIncTap = root.findViewById(R.id.btnRegularIncTap);
		regularIncTap.setOnClickListener(newOnRegularIncTapClickListener());

//		checkPurchasedItem(incTap, autoTap);
		coinsCounterTextView = root.findViewById(R.id.tvCoinsCounter);
		multiplayerCoinsCounterTextView = root.findViewById(R.id.tvMultiplayerCoinsCounter);
		currentLevelTextView = root.findViewById(R.id.tvCurrentLevel);
		shop = root.findViewById(R.id.btnShop);
		time = root.findViewById(R.id.time);
		progressBar = root.findViewById(R.id.progressBarB);
		donutImage = root.findViewById(R.id.donut);
		clock = root.findViewById(R.id.stopwatch);
		newClick = root.findViewById(R.id.newClick);
		newClick.setVisibility(View.INVISIBLE);

		timeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.timer_animation);
		rotateAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.donut_rotate);
		donutClickAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.donut_on_click_animation);
		preparingProgressBar(this.currentLevel, this.clicks);
		loadDonutImage();
		System.out.println(this.coins);
		coinsCounterTextView.setText(String.format(Locale.ENGLISH, "Coins: %d", this.coins));
		multiplayerCoinsCounterTextView.setText(String.format(Locale.ENGLISH, "Multicoins: %d", this.multiplayerCoins));
		// Click sound
		clickSound = new SoundPool.Builder().setMaxStreams(1).build();
		clickSound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int i, int i1) {
				if (cookieSettings.contains(PREFERENCES_VOLUME_EFFECT)) {
					volumeE = cookieSettings.getFloat(PREFERENCES_VOLUME_EFFECT,0);
				}

			}
		});
		clickSoundId = clickSound.load(getActivity(), R.raw.muda, 1);

		// Background sound
		backgroundSound = new SoundPool.Builder().setMaxStreams(1).build();
		backgroundSound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int i, int i1) {
				if (cookieSettings.contains(PREFERENCES_VOLUME_MUSIC))
				{
					volumeM = cookieSettings.getFloat(PREFERENCES_VOLUME_MUSIC, 0);
				}
				backgroundSound.play(backgroundSoundId, volumeM, volumeM, 0, -1, 1);

			}
		});

		backgroundSoundId = backgroundSound.load(this.getContext(), R.raw.epic_sax_guy_v6, 1);


		donutClickAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				donutClick();
				newClick.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				newClick.setVisibility(View.INVISIBLE);
				donutImage.startAnimation(rotateAnimation);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});


		donutImage.setOnClickListener(newOnDonutClickListener());

		initializeActivityState(savedInstanceState);

		return root;
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			loadInstanceState(savedInstanceState);
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("dpc_state", donutPerClick);
		outState.putInt("clicks_state", clicks);
		outState.putBoolean("flagShop_state", flagShop);
		outState.putLong("currentTime_state", currentTime);
	}

	@Override
	public void onResume() {
		super.onResume();
		backgroundSound.resume(1);
		coinsCounterTextView.setText(String.format(Locale.ENGLISH, "Coins: %d", coins));
		multiplayerCoinsCounterTextView.setText(String.format(Locale.ENGLISH, "Multicoins: %d", multiplayerCoins));
		donutImage.startAnimation(rotateAnimation);
		coinsCounterTextView.setTextColor(getResources().getColor(R.color.colorPoint));
		multiplayerCoinsCounterTextView.setTextColor(getResources().getColor(R.color.colorPoint));
		currentLevelTextView.setTextColor(getResources().getColor(R.color.colorPoint));
		if (currentTime != 0) {
			timer = currentTime;
		} else {
			timer = 60000;
		}
		countDownTimer = newCountDownTimer();
	}

	@Override
	public void onPause() {
		super.onPause();
		donutImage.clearAnimation();
		countDownTimer.cancel();
		if (flagShop) {
			countDownTimerBoost.cancel();
		}
		backgroundSound.pause(1);
		saveClicks();
	}

	@Override
	public void setPresenter(GameContract.Presenter presenter) {
		Preconditions.checkNotNull(presenter);
		this.presenter = presenter;
	}

	@Override
	public void showEndGameDialog() {

	}

	@Override
	public Context getActivityContext() {
		return this.getActivity();
	}

	@Override
	public void setClicks(Integer clicks) {
		coinsCounterTextView.setText(String.format(Locale.ENGLISH, "Coins: %d", this.coins)); // TODO: WTF!!1 Propblem with clicks text view
		multiplayerCoinsCounterTextView.setText(String.format(Locale.ENGLISH, "Multicoins: %d", this.multiplayerCoins)); // TODO: WTF!!1 Propblem with clicks text view
	}

	@Override
	public void setNewClick(Integer donutPerClick) {
		newClick.setText(String.format(Locale.ENGLISH, "+%d", this.donutPerClick));
	}

	@Override
	public void errorMultiplayer(Throwable e) {
		ErrorMultiplayerDialogFragment dialogFragment = ErrorMultiplayerDialogFragment.newInstance(e);
		FragmentManager manager = getFragmentManager();
		if (manager != null) {
			dialogFragment.show(getFragmentManager(), DIALOG_ERROR);
		} else {
			Log.e(TAG, "errorMultiplayer: FRAGMENT MANGER IS NULL!!!");
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null) {
			return;
		}
		donutPerClick = data.getIntExtra("donutPerClick", donutPerClick);
		clicks = data.getIntExtra("clicks", clicks);
		flagShop = data.getBooleanExtra("flagShop", flagShop);
		currentTime = data.getLongExtra("currentTime", currentTime);
	}

	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

	}

	private void initializeActivityState(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
//			donutPerClick = 1;
//			clicks = 0;
			flagShop = false;
			currentTime = 0;
		} else {
			loadInstanceState(savedInstanceState);
		}
	}

	private void loadInstanceState(Bundle savedInstanceState) {
		donutPerClick = savedInstanceState.getInt("dpc_state");
		clicks = savedInstanceState.getInt("clicks_state");
		flagShop = savedInstanceState.getBoolean("flagShop_state");
		currentTime = savedInstanceState.getLong("currentTime_state");
	}

	public CountDownTimer newCountDownTimer() {
		return new CountDownTimer(timer, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				if (millisUntilFinished <= 20000) {
					clock.startAnimation(timeAnimation);
				}
				clock.setText(String.format(Locale.getDefault(), TIMER_TIME_FORMAT,
						TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
								TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
						TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
								TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)))
				);
				currentTime = millisUntilFinished;
			}

			@Override
			public void onFinish() {
				if (flagShop) {
					countDownTimerBoost.cancel();
				}

				donutImage.clearAnimation();
				clock.setText(R.string.endGame);
				/*TODO: get message from End game dialog fragment*/
				String message = String.format(Locale.ENGLISH, "You earned %d clicks", clicks);

				presenter.finishGame(message, 100); // TODO: set level
//				dialog.setMessage(message);
//				dialog.show();
			}
		};
	}


	private void donutClick() {
		this.clicks += this.donutPerClick;
		increaseProgressBar(this.donutPerClick);
//		click.setText(Integer.toString(coins));
		newClick.setText(String.format(Locale.ENGLISH, "+%d", this.donutPerClick));
	}

	public View.OnClickListener newOnDonutClickListener() {
		return view -> {
			presenter.handleClick();
			clickSound.play(clickSoundId, volumeE, volumeE, 0, 0, 1);
			view.startAnimation(donutClickAnimation);
		};
	}

	public View.OnClickListener newOnIncrementTapClickListener() {
		return view -> {
			if (coins >= 10) {
				updateCoins(10);
				coinsCounterTextView.setText(String.format(Locale.ENGLISH, "Coins: %d", this.coins));
				this.donutPerClick += 1;
				new CountDownTimer(10000, 1000) {

					@Override
					public void onTick(long l) {

					}

					@Override
					public void onFinish() {
						donutPerClick -= 1;
					}
				}.start();
			}
		};
	}

	public View.OnClickListener newOnAutoTapClickListener() {
		return view -> {
			if (coins >= 20) {
				updateCoins(20);
				coinsCounterTextView.setText(String.format(Locale.ENGLISH, "Coins: %d", this.coins));
				new CountDownTimer(10000, 1000) {

					@Override
					public void onTick(long l) {
						donutClick();
					}

					@Override
					public void onFinish() {
					}
				}.start();
			}
		};
	}

	public View.OnClickListener newOnRegularAutoTapClickListener() {
		return view -> {
			if (multiplayerCoins >= 20) {
				mAutoClicks += 1;
				saves.edit().putInt("mAutoClicks", mAutoClicks).apply();
				updateMultiplayerCoins(20);
				multiplayerAutoClicks();

				multiplayerCoinsCounterTextView.setText(String.format(Locale.ENGLISH, "Multicoins: %d", this.multiplayerCoins));
			}
		};
	}

	public View.OnClickListener newOnRegularIncTapClickListener() {
		return view -> {
			if (multiplayerCoins >= 10) {
				donutPerClick += 1;
				saves.edit().putInt("donutPerClick", donutPerClick).apply();
				updateMultiplayerCoins(10);

				multiplayerCoinsCounterTextView.setText(String.format(Locale.ENGLISH, "Multicoins: %d", this.multiplayerCoins));
			}
		};
	}

	private void loadGameSaves() {
		this.donutPerClick = saves.getInt("donutPerClick", 0);
		this.tempClicks = saves.getInt("tempClicks", 0);
		this.tempAutoClicks = saves.getInt("tempAutoClicks", 0);
		this.mAutoClicks = saves.getInt("mAutoClicks", 0);
		this.currentLevel = saves.getInt("currentLevel", 0);
		this.coins = saves.getInt("commonCoins", 0);
		this.multiplayerCoins = saves.getInt("multiplayerCoins", 0);
		this.clicks = saves.getInt("clicks", 0);
	}

	private void loadDonutImage() {
		donutImage.setBackground(null);
		this.donutImage.setImageResource(getContext()
				.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
				.getInt(PREFERENCES_DONUT_ID, ImageDonut.PINK_DONUT.resourceId));
	}


	private void saveClicks() {
		saves = getContext().getSharedPreferences(getString(R.string.gameSaves), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = saves.edit();
		editor.putInt("clicks", (int)progressBar.getProgress());
		editor.putInt("commonCoins", this.coins);
		editor.putInt("currentLevel", this.currentLevel);
		editor.apply();
	}

	private void multiplayerAutoClicks() {

		new CountDownTimer(999999999, 1000) {
			@Override
			public void onTick(long l) {
				donutClick();
			}

			@Override
			public void onFinish() {
			}
		}.start();
	}

	private void startAutoClicks(int autoClicks) {
		if (autoClicks > 0) {
			for (int i = 0; i < autoClicks; i++) {
				multiplayerAutoClicks();
			}
		}
	}

	private void preparingProgressBar(int curentLevel, int clicks) {
		requiredClicks = (int)Math.pow(((float)(curentLevel + 1) / 0.5), 2);
		progressBar.setMax(requiredClicks);
		progressBar.setProgress(clicks);
		currentLevelTextView.setText(getResources().getString(R.string.current_level) + " " + this.currentLevel);
	}

	private void increaseProgressBar(int value) {
		progressBar.setProgress((int)progressBar.getProgress() + value);
		calculatingNextLevelState();
	}

	private void calculatingNextLevelState() {
		if (this.clicks > requiredClicks) {
			this.clicks = this.clicks - requiredClicks;
			currentLevel += 1;
			this.coins += 10;

			String message = String.format(Locale.ENGLISH, "You earned level %d", this.currentLevel);
			presenter.finishGame(message, this.requiredClicks);

			preparingProgressBar(currentLevel, this.clicks);
			coinsCounterTextView.setText(String.format(Locale.ENGLISH, "Coins: %d", this.coins));
//			multiplayerCoinsCounterTextView.setText(String.format(Locale.ENGLISH, "Multicoins: %d", this.multiplayerCoins)); // TODO for multiplayer add "If" condition


		} else if (this.clicks == requiredClicks) {
			currentLevel += 1;
			this.coins += 10;
			this.clicks = 0;

			String message = String.format(Locale.ENGLISH, "You earned level %d ", this.currentLevel);
			presenter.finishGame(message, this.requiredClicks);

			preparingProgressBar(currentLevel, 0);
			coinsCounterTextView.setText(String.format(Locale.ENGLISH, "Coins: %d", this.coins));
			multiplayerCoinsCounterTextView.setText(String.format(Locale.ENGLISH, "Multicoins: %d", this.multiplayerCoins)); // TODO for multiplayer add "If" condition


		}
	}

	private void updateCoins(int coins) {
		this.coins -= coins;

	}

	private void updateMultiplayerCoins(int coins) {
		this.multiplayerCoins -= coins;
	}
}
