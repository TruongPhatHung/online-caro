import { useState, useEffect } from 'react';
import axios from 'axios';
import { User, Key, Users, Grid, PlusCircle, LogIn, Trophy, Swords, BarChart3, Clock, AlertCircle, CheckCircle2, AlertTriangle, X } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid } from 'recharts';

const isLocalhost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
const BACKEND_URL = isLocalhost 
    ? 'http://localhost:8080' 
    : 'https://6bxqtc2n-8080.asse.devtunnels.ms';

const API_BASE_URL = `${BACKEND_URL}/api/rooms`;
const STATS_API_URL = `${BACKEND_URL}/api/stats`;

function Lobby({ onRoomJoined }) {
    const [playerName, setPlayerName] = useState('');
    const [roomId, setRoomId] = useState('');
    const [leaderboard, setLeaderboard] = useState([]);
    const [history, setHistory] = useState([]);
    const [maxPlayers, setMaxPlayers] = useState(2);
    const [boardSize, setBoardSize] = useState(15);

    // State quản lý Toast Thông Báo
    const [toast, setToast] = useState(null);

    const showToast = (message, type = 'warning') => {
        setToast({ message, type });
        setTimeout(() => setToast(null), 3500);
    };

    useEffect(() => {
        fetchStats();
    }, []);

    const fetchStats = async () => {
        try {
            const [leaderboardRes, historyRes] = await Promise.all([
                axios.get(`${STATS_API_URL}/leaderboard`),
                axios.get(`${STATS_API_URL}/history`)
            ]);
            setLeaderboard(leaderboardRes.data);
            setHistory(historyRes.data);
        } catch (error) {
            console.error("Lỗi khi tải dữ liệu thống kê:", error);
        }
    };

    const handleCreateRoom = async () => {
        if (!playerName.trim()) return showToast("Vui lòng nhập tên của bạn!", "warning");
        try {
            const response = await axios.post(`${API_BASE_URL}/create`, { 
                playerName, maxPlayers, boardSize 
            });
            onRoomJoined(response.data, playerName);
        } catch (error) {
            showToast("Lỗi tạo phòng, vui lòng thử lại!", "error");
            console.error(error);
        }
    };

    const handleJoinRoom = async () => {
        if (!playerName.trim() || !roomId.trim()) {
            return showToast("Vui lòng nhập tên và mã phòng!", "warning");
        }
        try {
            const response = await axios.post(`${API_BASE_URL}/join`, { roomId, playerName });
            onRoomJoined(response.data, playerName);
        } catch (error) {
            showToast("Không tìm thấy phòng hoặc phòng đã đầy!", "error");
        }
    };

    // Bổ sung chỉ số Hòa vào Dữ liệu Biểu đồ
    const chartData = leaderboard.slice(0, 5).map(p => ({
        name: p.playerName,
        'Thắng': p.wins || 0,
        'Hòa': p.draws || 0,
        'Thua': p.losses || 0,
    }));

    return (
        <div className="lobby-wrapper">
            {/* THÔNG BÁO TOAST UI */}
            {toast && (
                <div className="toast-container">
                    <div className={`toast-card ${toast.type}`}>
                        <div className="toast-icon">
                            {toast.type === 'error' && <AlertCircle size={20} />}
                            {toast.type === 'warning' && <AlertTriangle size={20} />}
                            {toast.type === 'success' && <CheckCircle2 size={20} />}
                        </div>
                        <span>{toast.message}</span>
                        <button className="toast-close" onClick={() => setToast(null)}>
                            <X size={16} />
                        </button>
                    </div>
                </div>
            )}

            {/* Cột trái: Form Tạo/Vào phòng */}
            <div className="lobby-main modern-card">
                <div className="lobby-header">
                    <Swords size={32} className="text-primary" />
                    <h2>Sảnh Chờ Caro</h2>
                </div>
                
                <div className="input-group">
                    <User size={18} className="input-icon" />
                    <input type="text" placeholder="Nhập tên của bạn..." value={playerName} onChange={(e) => setPlayerName(e.target.value)} />
                </div>
                
                <div className="room-settings">
                    <div className="select-wrapper">
                        <Users size={16} className="select-icon"/>
                        <select value={maxPlayers} onChange={(e) => setMaxPlayers(Number(e.target.value))}>
                            <option value={2}>2 Người chơi</option>
                            <option value={3}>3 Người chơi</option>
                        </select>
                    </div>
                    
                    <div className="select-wrapper">
                        <Grid size={16} className="select-icon"/>
                        <select value={boardSize} onChange={(e) => setBoardSize(Number(e.target.value))}>
                            <option value={15}>Bàn 15x15</option>
                            <option value={20}>Bàn 20x20</option>
                            <option value={25}>Bàn 25x25</option>
                        </select>
                    </div>
                </div>

                <button onClick={handleCreateRoom} className="action-btn create-btn">
                    <PlusCircle size={18} /> Tạo Phòng Mới
                </button>

                <div className="divider"><span>HOẶC</span></div>

                <div className="join-group">
                    <div className="input-group flex-1">
                        <Key size={18} className="input-icon" />
                        <input type="text" placeholder="Nhập mã phòng..." value={roomId} onChange={(e) => setRoomId(e.target.value)} />
                    </div>
                    <button onClick={handleJoinRoom} className="action-btn join-btn">
                        <LogIn size={18} /> Vào Phòng
                    </button>
                </div>
            </div>

            {/* Cột phải: Thống kê */}
            <div className="lobby-stats">
                <div className="stats-section modern-card">
                    <h3><BarChart3 size={20} className="text-win" /> Tỉ Lệ Thắng/Hòa/Thua Top 5</h3>
                    <div className="chart-container" style={{ width: '100%', height: 250, marginTop: '10px' }}>
                        <ResponsiveContainer>
                            <BarChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#444" vertical={false} />
                                <XAxis dataKey="name" stroke="#a4b0be" fontSize={12} />
                                <YAxis stroke="#a4b0be" fontSize={12} allowDecimals={false} />
                                <Tooltip cursor={{fill: 'rgba(255,255,255,0.05)'}} contentStyle={{ backgroundColor: '#2f3542', border: 'none', borderRadius: '8px', color: '#fff' }} />
                                <Legend />
                                <Bar dataKey="Thắng" fill="#2ed573" radius={[4, 4, 0, 0]} barSize={22} />
                                <Bar dataKey="Hòa" fill="#ffa502" radius={[4, 4, 0, 0]} barSize={22} />
                                <Bar dataKey="Thua" fill="#ff4757" radius={[4, 4, 0, 0]} barSize={22} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="stats-row">
                    <div className="stats-section modern-card half-width">
                        <h3><Trophy size={20} className="text-win" /> Bảng Xếp Hạng</h3>
                        <table className="stats-table">
                            <thead>
                                <tr>
                                    <th>Top</th>
                                    <th>Người chơi</th>
                                    <th>Thắng</th>
                                    <th>Hòa</th>
                                    <th>Thua</th>
                                </tr>
                            </thead>
                            <tbody>
                                {leaderboard.slice(0, 5).map((player, index) => (
                                    <tr key={player.playerName}>
                                        <td><span className={`rank-badge rank-${index + 1}`}>#{index + 1}</span></td>
                                        <td className="fw-bold">{player.playerName}</td>
                                        <td className="text-win">{player.wins || 0}</td>
                                        <td style={{ color: '#ffa502', fontWeight: 'bold' }}>{player.draws || 0}</td>
                                        <td className="text-lose">{player.losses || 0}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    <div className="stats-section modern-card half-width">
                        <h3><Clock size={20} className="text-primary" /> Lịch Sử Trận Đấu</h3>
                        <div className="table-scroll">
                            <table className="stats-table">
                                <thead>
                                    <tr>
                                        <th>Phòng</th>
                                        <th>Trận đấu</th>
                                        <th>Winner</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {history.slice(0, 5).map((match) => (
                                        <tr key={match.id}>
                                            <td className="text-muted">{match.roomId}</td>
                                            <td>{match.playerX} vs {match.playerO}</td>
                                            <td>
                                                <span 
                                                    className={
                                                        match.winner === 'CANCELLED' ? 'text-lose' : 
                                                        match.winner === 'DRAW' ? 'text-warning' : 'text-win'
                                                    }
                                                    style={match.winner === 'DRAW' ? { color: '#ffa502', fontWeight: 'bold' } : {}}
                                                >
                                                    {match.winner === 'CANCELLED' ? 'Hủy' : match.winner === 'DRAW' ? '🤝 Hòa' : match.winner}
                                                </span>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Lobby;