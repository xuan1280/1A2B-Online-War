package com.ood.clean.waterball.a1a2bsdk.core.modules.roomlist;

import android.support.annotation.NonNull;

import com.ood.clean.waterball.a1a2bsdk.core.CoreGameServer;
import com.ood.clean.waterball.a1a2bsdk.core.ModuleName;
import com.ood.clean.waterball.a1a2bsdk.core.base.AbstractGameModule;
import com.ood.clean.waterball.a1a2bsdk.core.base.BindCallback;
import com.ood.clean.waterball.a1a2bsdk.core.base.exceptions.CallbackException;
import com.ood.clean.waterball.a1a2bsdk.core.modules.signIn.UserSigningModule;

import java.util.List;

import container.protocol.Protocol;
import gamecore.entity.GameRoom;
import gamecore.model.GameMode;
import gamecore.model.RequestStatus;

import static container.Constants.Events.InRoom.CLOSE_ROOM;
import static container.Constants.Events.RoomList.CREATE_ROOM;
import static container.Constants.Events.RoomList.GET_ROOMS;
import static container.Constants.Events.RoomList.JOIN_ROOM;


public class RoomListModuleImp extends AbstractGameModule implements RoomListModule {
    private UserSigningModule signingModule;
    private ProxyCallback proxyCallback;

    public RoomListModuleImp() {
        this.signingModule = (UserSigningModule) CoreGameServer.getInstance().getModule(ModuleName.SIGNING);
    }

    @Override
    public void registerCallback(Callback callback) {
        if (this.proxyCallback != null)
            callback.onError(new CallbackException());
        this.proxyCallback = new ProxyCallback(callback);
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
    public void createRoom(String roomName, GameMode gameMode) {
        GameRoom gameRoom = new GameRoom(gameMode, roomName, signingModule.getCurrentPlayer());
        Protocol protocol = protocolFactory.createProtocol(CREATE_ROOM, RequestStatus.request.toString(), gson.toJson(gameRoom));
        client.respond(protocol);
    }


    @Override
    public void joinRoom(GameRoom gameRoom) {
        Protocol protocol = protocolFactory.createProtocol(JOIN_ROOM, RequestStatus.request.toString(), gson.toJson(gameRoom));
        client.respond(protocol);
    }

    @Override
    public void getGameRoomList() {
        Protocol protocol = protocolFactory.createProtocol(GET_ROOMS, RequestStatus.request.toString(), null);
        client.respond(protocol);
    }

    public class ProxyCallback implements RoomListModule.Callback{
        private RoomListModule.Callback callback;

        public ProxyCallback(Callback callback) {
            this.callback = callback;
        }

        @BindCallback(event = GET_ROOMS, status = RequestStatus.success)
        @Override
        public void onGetRoomList(List<GameRoom> gameRooms) {
            callback.onGetRoomList(gameRooms);
        }

        @BindCallback(event = CREATE_ROOM, status = RequestStatus.success)
        @Override
        public void onNewRoom(GameRoom gameRoom) {
            callback.onNewRoom(gameRoom);
        }

        @BindCallback(event = CLOSE_ROOM, status = RequestStatus.success)
        @Override
        public void onRoomClosed(GameRoom gameRoom) {
            callback.onRoomClosed(gameRoom);
        }

        @BindCallback(event = "UpdateRoom", status = RequestStatus.success)
        @Override
        public void onRoomUpdated(GameRoom gameRoom) {
            callback.onRoomUpdated(gameRoom);
        }


        @BindCallback(event = JOIN_ROOM, status = RequestStatus.success)
        @Override
        public void onJoinRoomSuccessfully(GameRoom gameRoom) {
            callback.onJoinRoomSuccessfully(gameRoom);
        }

        @Override
        public void onError(@NonNull Throwable err) {
            callback.onError(err);
        }
    }

}
