import React from 'react';

export default function Board({ board, onSquareClick, winningLine }) {
    const isWinningSquare = (r, c) => {
        if (!winningLine) return false;
        return winningLine.some(cell => cell[0] === r && cell[1] === c);
    };

    // Lấy kích thước bàn cờ (15, 20, hoặc 25)
    const boardSize = board.length;

    return (
       
        <div className="board" style={{ '--board-size': boardSize }}>
            {board.map((row, rIndex) => (
                <div key={rIndex} className="board-row">
                    {row.map((cell, cIndex) => (
                        <button 
                            key={cIndex} 
                            className={`square ${cell === 'X' ? 'text-x' : cell === 'O' ? 'text-o' : cell === 'Y' ? 'text-y' : ''} ${isWinningSquare(rIndex, cIndex) ? 'highlight' : ''}`}
                            onClick={() => onSquareClick(rIndex, cIndex)}
                        >
                            {cell}
                        </button>
                    ))}
                </div>
            ))}
        </div>
    );
}