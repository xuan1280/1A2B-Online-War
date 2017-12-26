package com.ood.clean.waterball.a1a2bsdk.core.modules.games;


import android.support.annotation.NonNull;
import android.util.Log;

import com.ood.clean.waterball.a1a2bsdk.core.CoreGameServer;
import com.ood.clean.waterball.a1a2bsdk.core.ModuleName;
import com.ood.clean.waterball.a1a2bsdk.core.base.AbstractGameModule;
import com.ood.clean.waterball.a1a2bsdk.core.base.BindCallback;
import com.ood.clean.waterball.a1a2bsdk.core.base.exceptions.CallbackException;
import com.ood.clean.waterball.a1a2bsdk.core.modules.roomlist.RoomListModuleImp;
import com.ood.clean.waterball.a1a2bsdk.core.modules.signIn.UserSigningModule;

import java.util.List;

import container.protocol.Protocol;
import gamecore.model.ContentModel;
import gamecore.model.RequestStatus;
import gamecore.model.games.a1b2.Duel1A2BPlayerBarModel;
import gamecore.model.games.a1b2.GameOverModel;

import static container.Constants.Events.Games.Duel1A2B.GUESS;
import static container.Constants.Events.Games.Duel1A2B.GUESSING_STARTED;
import static container.Constants.Events.Games.Duel1A2B.ONE_ROUND_OVER;
import static container.Constants.Events.Games.Duel1A2B.SET_ANSWER;
import static container.Constants.Events.Games.GAMEOVER;

public class Duel1A2BModuleImp extends AbstractGameModule implements Due11A2BModule{
    private UserSigningModule signingModule;
    private RoomListModuleImp roomListModuleImp;
    private ProxyCallback proxyCallback;

    public Duel1A2BModuleImp(){
        signingModule = (UserSigningModule) CoreGameServer.getInstance().getModule(ModuleName.SIGNING);
        roomListModuleImp = (RoomListModuleImp) CoreGameServer.getInstance().getModule(ModuleName.ROOMLIST);
    }

    @Override
    public void registerCallback(Callback callback) {
        if (this.proxyCallback != null)
            callback.onError(new CallbackException());
        this.proxyCallback = new Duel1A2BModuleImp.ProxyCallback(callback);
        eventBus.registerCallback(proxyCallback);
    }

    @Override
    public void unregisterCallBack(Callback callback) {
        if (this.proxyCallback == null || this.proxyCallback.callback != callback)
            callback.onError(new CallbackException());
        eventBus.unregisterCallback(proxyCallback);
        this.proxyCallback = null;
    }

    @Override
    public void setAnswer(String answer) {
        Protocol protocol = protocolFactory.createProtocol(SET_ANSWER,
                RequestStatus.request.toString(), gson.toJson(new ContentModel(
                        signingModule.getCurrentPlayer().getId(), roomListModuleImp.getCurrentGameRoom().getId(), answer)));
        client.broadcast(protocol);
    }

    @Override
    public void guess(String guess) {
        Protocol protocol = protocolFactory.createProtocol(GUESS,
                RequestStatus.request.toString(), gson.toJson(new ContentModel(
                        signingModule.getCurrentPlayer().getId(), roomListModuleImp.getCurrentGameRoom().getId(), guess)));
        client.broadcast(protocol);
    }

    public class ProxyCallback implements Due11A2BModule.Callback{
        private Due11A2BModule.Callback callback;

        public ProxyCallback(Due11A2BModule.Callback callback) {
            this.callback = callback;
        }

        @Override
        @BindCallback(event = SET_ANSWER, status = RequestStatus.success)
        public void onSetAnswerSuccessfully(ContentModel setAnswerModel) {
            Log.d(TAG, "answer set: " + setAnswerModel.getContent());
            callback.onSetAnswerSuccessfully(setAnswerModel);
        }

        @Override
        @BindCallback(event = GUESS, status = RequestStatus.success)
        public void onGuessSuccessfully(ContentModel guessModel) {
            Log.d(TAG, "guessed : " + guessModel.getContent());
            callback.onGuessSuccessfully(guessModel);
        }

        @Override
        @BindCallback(event = GUESSING_STARTED, status = RequestStatus.success)
        public void onGuessingStarted() {
            Log.d(TAG, "guessing phase started.");
            callback.onGuessingStarted();
        }

        @Override
        @BindCallback(event = ONE_ROUND_OVER, status = RequestStatus.success)
        public void onOneRoundOver(List<Duel1A2BPlayerBarModel> models) {
            Log.d(TAG, "One round over.");
            callback.onOneRoundOver(models);
        }

        @Override
        @BindCallback(event = GAMEOVER, status = RequestStatus.success)
        public void onGameOver(GameOverModel gameOverModel) {
            Log.d(TAG, "Game over, the winner's id is: " + gameOverModel.getWinnerId());
            callback.onGameOver(gameOverModel);
        }

        @Override
        public void onError(@NonNull Throwable err) {
            callback.onError(err);
        }
    }
}