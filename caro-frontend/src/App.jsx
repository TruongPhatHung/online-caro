import { useState } from 'react';
import Lobby from './components/Lobby';
import Game from './components/Game';
import './App.css';

function App() {
  const [roomInfo, setRoomInfo] = useState(null);
  const [currentPlayer, setCurrentPlayer] = useState('');

  const handleRoomJoined = (roomData, playerName) => {
    setRoomInfo(roomData);
    setCurrentPlayer(playerName);
  };

  return (
    <div className="App">
      {!roomInfo ? (
        <Lobby onRoomJoined={handleRoomJoined} />
      ) : (
        // Đã bổ sung onLeaveRoom={() => setRoomInfo(null)} vào đây!
        <Game 
          roomInfo={roomInfo} 
          currentPlayer={currentPlayer} 
          onLeaveRoom={() => setRoomInfo(null)} 
        />
      )}
    </div>
  );
}

export default App;