import { Router, Request, Response, NextFunction } from 'express'
import jwt from 'jsonwebtoken'

interface AuthRequest extends Request {
    user?: {
        id: number;
        email: string;
    }
}

const authMiddleware = (req: AuthRequest, res: Response, next: NextFunction) => {
    const authHeader = req.headers.authorization

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ message: 'No token provided' })
    }

    const token = authHeader.split(' ')[1] as string

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET!) as { id: number; email: string }
        req.user = decoded
        next()
    }
    catch (error) {
        return res.status(401).json({ message: 'Invalid or expired token' })
    }
}

export default authMiddleware