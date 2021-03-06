package ru.rienel.clicker.activity.game;

import android.content.Context;

import ru.rienel.clicker.activity.BasePresenter;
import ru.rienel.clicker.activity.BaseView;

public interface GameContract {
	interface View extends BaseView<Presenter> {
		void showEndGameDialog();

		Context getActivityContext();

		void setClicks(Integer clicks);

		void setNewClick(Integer donutPerClick);

		void errorMultiplayer(Throwable e);
	}

	interface Presenter extends BasePresenter {
		void startGame();

		void sendSignalToOpponent();

		void handleClick();

		void finishGame(String message, Integer goal);
	}
}
