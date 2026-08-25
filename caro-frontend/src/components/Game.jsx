import { useEffect, useState, useRef } from 'react';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import Board from './Board';
import Confetti from 'react-confetti';
import { Trophy, Skull, RotateCcw, LogOut, Users, AlertCircle, Hourglass, X, AlertTriangle, CheckCircle2, Info } from 'lucide-react';

export default function Game({ roomInfo, currentPlayer, onLeaveRoom }) {
    const [room, setRoom] = useState(roomInfo);
    const [stompClient, setStompClient] = useState(null);
    const [timeLeft, setTimeLeft] = useState(120);
    const [droppedPlayer, setDroppedPlayer] = useState(null); 
    
    // --- QUẢN LÝ TOAST NOTIFICATION ---
    const [toast, setToast] = useState(null);
    const toastTimerRef = useRef(null);

    const showToast = (message, type = 'warning') => {
        if (toastTimerRef.current) clearTimeout(toastTimerRef.current);
        setToast({ message, type });
        toastTimerRef.current = setTimeout(() => setToast(null), 3500); // Ẩn sau 3.5 giây
    };

    const mySymbol = currentPlayer === room.playerX ? 'X' 
                   : currentPlayer === room.playerO ? 'O' 
                   : 'Y';
                   
    const isMyTurn = room.currentTurn === mySymbol;
    const isGameStarted = room.board.some(row => row.some(cell => cell !== null));

    const activePlayers = [room.playerX, room.playerO, room.playerY].filter(Boolean);
    const activePlayersCount = activePlayers.length;
    
    const prevPlayersRef = useRef(activePlayers);

    useEffect(() => {
        const isLocalhost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
        const WS_URL = isLocalhost 
            ? 'http://localhost:8080/ws' 
            : 'https://6bxqtc2n-8080.asse.devtunnels.ms/ws';
            
        const socket = new SockJS(WS_URL);
        const client = Stomp.over(socket);
        
        client.connect({}, () => {
            client.send('/app/game.register', {}, JSON.stringify({ roomId: roomInfo.roomId, playerName: currentPlayer }));

            client.subscribe(`/topic/room/${roomInfo.roomId}`, (message) => {
                setRoom(JSON.parse(message.body));
                setTimeLeft(120);
            });

            client.subscribe(`/topic/room/${roomInfo.roomId}/system`, (message) => {
                // Đã xóa emoji ⚠️ để không bị trùng icon
                showToast(message.body, 'info');
            });
        });

        setStompClient(client);
        
        return () => {
            if (client && client.connected) {
                client.disconnect();
            }
        };
    }, [roomInfo.roomId, currentPlayer]);

    // Theo dõi người rời phòng
    useEffect(() => {
        const currentPlayers = [room.playerX, room.playerO, room.playerY].filter(Boolean);
        const prevPlayers = prevPlayersRef.current;

        const leftPlayer = prevPlayers.find(p => !currentPlayers.includes(p));
        if (leftPlayer) {
            setDroppedPlayer(leftPlayer);
            showToast(`Người chơi ${leftPlayer} đã rời phòng!`, 'error');
        }
        prevPlayersRef.current = currentPlayers;
    }, [room.playerX, room.playerO, room.playerY]);

    useEffect(() => {
        if (room.winner === 'CANCELLED') {
            setTimeLeft(0);
        }
    }, [room.winner]);

    useEffect(() => {
        if (room.winner || room.isGameOver || !isGameStarted) return;
        const timer = setInterval(() => {
            setTimeLeft((prevTime) => {
                if (prevTime <= 1) {
                    clearInterval(timer);
                    if (stompClient && isMyTurn) stompClient.send('/app/game.timeout', {}, room.roomId);
                    return 0;
                }
                return prevTime - 1;
            });
        }, 1000);
        return () => clearInterval(timer);
    }, [room.currentTurn, room.winner, room.isGameOver, isGameStarted, stompClient, room.roomId, isMyTurn]);

    const handleSquareClick = (row, col) => {
        if (room.winner || room.isGameOver || room.board[row][col]) return; 
        
        if (!isMyTurn) {
            showToast("Chưa tới lượt của bạn!", "warning");
            return;
        }

        if (!isGameStarted && room.maxPlayers && activePlayersCount < room.maxPlayers) {
            // Đã xóa emoji ⚠️ ở đây
            showToast(`Chưa đủ ${room.maxPlayers} người chơi để bắt đầu (hiện có ${activePlayersCount}/${room.maxPlayers}).`, "warning");
            return;
        }

        const moveMessage = { roomId: room.roomId, playerName: currentPlayer, row, col };
        stompClient.send('/app/game.move', {}, JSON.stringify(moveMessage));
    };

    const handleLeave = () => {
        if (stompClient) {
            stompClient.send('/app/game.leave', {}, JSON.stringify({ roomId: room.roomId, playerName: currentPlayer }));
        }
        if (onLeaveRoom) onLeaveRoom(); 
    };

    const formatTime = (seconds) => {
        const m = Math.floor(seconds / 60);
        const s = seconds % 60;
        return `${m}:${s < 10 ? '0' : ''}${s}`;
    };

    const getWinnerName = (symbol) => {
        if (symbol === 'X') return room.playerX;
        if (symbol === 'O') return room.playerO;
        if (symbol === 'Y') return room.playerY || 'Người chơi 3';
        return symbol;
    };

    const getPlayerName = (symbol) => {
        if (symbol === 'X') return room.playerX || 'X';
        if (symbol === 'O') return room.playerO || 'O';
        if (symbol === 'Y') return room.playerY || 'Y';
        return symbol;
    };

    const isCancelled = room.winner === 'CANCELLED';

    return (
        <div className="game-container">
            {/* THÔNG BÁO TOAST UI CHUYÊN NGHIỆP */}
            {toast && (
                <div className="toast-container">
                    <div className={`toast-card ${toast.type}`}>
                        <div className="toast-icon">
                            {toast.type === 'error' && <AlertCircle size={20} />}
                            {toast.type === 'warning' && <AlertTriangle size={20} />}
                            {toast.type === 'success' && <CheckCircle2 size={20} />}
                            {toast.type === 'info' && <Info size={20} />}
                        </div>
                        <span className="toast-message">{toast.message}</span>
                        <button className="toast-close" onClick={() => setToast(null)}>
                            <X size={16} />
                        </button>
                    </div>
                </div>
            )}

            {room.winner === mySymbol && (
                <Confetti width={window.innerWidth} height={window.innerHeight} recycle={false} numberOfPieces={500} gravity={0.15} />
            )}

            <div className="game-header">
                <button className="leave-btn modern-btn" onClick={handleLeave}>
                    <LogOut size={18} /> Thoát Phòng
                </button>
                <div className="player-count modern-badge">
                    <Users size={18} /> Số người: {activePlayersCount}
                </div>
            </div>

            <h2 className="room-title">Phòng: {room.roomId} - Người chơi: {currentPlayer} ({mySymbol})</h2>

            {isCancelled ? (
                <div className="winner-section" style={{ 
                    padding: '2rem', 
                    border: '2px dashed #ff4757', 
                    borderRadius: '12px', 
                    backgroundColor: 'rgba(255, 71, 87, 0.05)',
                    marginTop: '20px'
                }}>
                    <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '15px' }}>
                        <AlertCircle size={56} color="#ff4757" className="bounce-icon" />
                    </div>
                    <h3 className="winner-text error-text" style={{ fontSize: '1.6rem', marginBottom: '15px', color: '#ff4757' }}>
                        TRẬN ĐẤU BỊ HỦY!
                    </h3>
                    <p style={{ fontSize: '1.1rem', color: '#ff6b81', marginBottom: '25px', lineHeight: '1.6' }}>
                        Người chơi <strong style={{ color: '#ff4757', fontSize: '1.2rem', textTransform: 'uppercase' }}>{droppedPlayer || "khác"}</strong> đã rời phòng.<br/>
                        Số lượng người chơi không đủ để tiếp tục ván đấu này.
                    </p>
                    <button onClick={handleLeave} className="reset-btn modern-btn" style={{ padding: '12px 28px' }}>
                        <LogOut size={18} /> Quay Lại Sảnh Nghỉ
                    </button>
                </div>
            ) : room.winner && room.winner !== 'CANCELLED' ? (
                <div className="winner-section">
                    <h3 className={`winner-text ${room.winner === mySymbol ? 'win-text' : 'lose-text'}`}>
                        {room.winner === mySymbol ? (
                            <>
                                <Trophy size={28} className="bounce-icon" /> 
                                <span>BẠN ĐÃ CHIẾN THẮNG!</span> 
                                <Trophy size={28} className="bounce-icon" />
                            </>
                        ) : (
                            <>
                                <Skull size={28} /> 
                                <span>{getWinnerName(room.winner).toUpperCase()} ĐÃ THẮNG!</span>
                            </>
                        )}
                    </h3>
                    <button onClick={() => stompClient.send('/app/game.reset', {}, room.roomId)} className="reset-btn modern-btn success-btn">
                        <RotateCcw size={18} /> Chơi Ván Mới
                    </button>
                </div>
            ) : (
                <div className="turn-section">
                    <h3>
                        Lượt đánh: <span className={`text-${room.currentTurn?.toLowerCase()}`}>
                            {isMyTurn ? "Bạn" : `${getPlayerName(room.currentTurn)} (${room.currentTurn})`}
                        </span>
                    </h3>
                    {isGameStarted ? (
                        <h3 className="timer-text" style={{ color: timeLeft <= 30 ? '#ff4757' : '#2ed573' }}>
                            <Hourglass size={18} /> Thời gian {isMyTurn ? "của bạn" : `của ${getPlayerName(room.currentTurn)}`}: {formatTime(timeLeft)}
                        </h3>
                    ) : (
                        <h3 style={{ color: 'gray' }}>Đang chờ nước đi đầu tiên...</h3>
                    )}
                </div>
            )}
            
            <Board board={room.board} onSquareClick={handleSquareClick} winningLine={room.winningLine} />
        </div>
    );
}