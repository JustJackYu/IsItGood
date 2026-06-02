import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';

dotenv.config();

import authRouter from './routes/auth';
import gamesRouter from './routes/games';
import chatRouter from './routes/chat';


const app = express();
app.use(cors());
app.use(express.json());
app.use('/auth', authRouter);
app.use('/games', gamesRouter);
app.use('/chat', chatRouter);

app.get('/health', (req, res) => {
    res.json({ status: 'ok', project: 'IsItGood?' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`IsItGood? backend running on port ${PORT}`);
});