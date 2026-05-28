import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import authRouter from './routes/auth';
import gamesRouter from './routes/games';

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());
app.use('/auth', authRouter);
app.use('/games', gamesRouter);

app.get('/health', (req, res) => {
    res.json({ status: 'ok', project: 'IsItGood?' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`IsItGood? backend running on port ${PORT}`);
});