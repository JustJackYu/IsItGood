import { Router, Request, Response } from 'express'
import prisma from '../prisma/client'
import bcrypt from 'bcrypt'
import jwt from 'jsonwebtoken'
import authMiddleware from '../middleware/auth'

interface AuthRequest extends Request {
    user?: {
        id: number;
        email: string;
    }
}

const router = Router()

// How long an issued JWT stays valid. Mirrors the client's 30-day persisted session.
const TOKEN_EXPIRY = '30d'

const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*\-_]).{8,}$/

const validatePassword = (password: string): string | null => {
    if (!password || password.length < 8) return 'Password must be at least 8 characters'
    if (password.length > 128) return 'Password must be under 128 characters'
    if (!passwordRegex.test(password)) {
        return 'Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character (!@#$%^&*-_)'
    }
    return null
}

router.post('/register', async (req: Request, res: Response) => {
    const { email, password, username } = req.body

    if (!email || !password) {
        return res.status(400).json({ message: 'Email and password are required' })
    }

    if (!username || typeof username !== 'string' || username.trim().length === 0) {
        return res.status(400).json({ message: 'Username is required' })
    }

    if (username.trim().length > 30) {
        return res.status(400).json({ message: 'Username must be 30 characters or less' })
    }

    const passwordError = validatePassword(password)
    if (passwordError) {
        return res.status(400).json({ message: passwordError })
    }

    try {
        const existingUser = await prisma.user.findUnique({ where: { email } })
        if (existingUser) {
            return res.status(400).json({ message: 'Email already in use' })
        }

        const hashedPassword = await bcrypt.hash(password, 10)

        const newUser = await prisma.user.create({
            data: { email, password: hashedPassword, username: username.trim() }
        })

        const token = jwt.sign(
            { id: newUser.id, email: newUser.email },
            process.env.JWT_SECRET!,
            { expiresIn: TOKEN_EXPIRY }
        )

        return res.status(200).json({ token })
    } catch (error) {
        console.error('Error registering user:', error)
        return res.status(500).json({ message: 'Internal Server Error' })
    }
})

router.post('/login', async (req: Request, res: Response) => {
    const { email, password } = req.body

    if (!email || !password) {
        return res.status(400).json({ message: 'Email and password are required' })
    }

    try {
        const user = await prisma.user.findUnique({ where: { email } })
        if (!user) {
            return res.status(400).json({ message: 'Invalid credentials' })
        }

        const isMatch = await bcrypt.compare(password, user.password)
        if (!isMatch) {
            return res.status(400).json({ message: 'Invalid credentials' })
        }

        const token = jwt.sign(
            { id: user.id, email: user.email },
            process.env.JWT_SECRET!,
            { expiresIn: TOKEN_EXPIRY }
        )

        return res.status(200).json({ token })
    } catch (error) {
        return res.status(500).json({ message: 'Internal Server Error' })
    }
})

router.post('/change-password', authMiddleware, async (req: AuthRequest, res: Response) => {
    const { currentPassword, newPassword } = req.body

    if (!currentPassword || !newPassword) {
        return res.status(400).json({ message: 'Current and new password are required' })
    }

    const passwordError = validatePassword(newPassword)
    if (passwordError) {
        return res.status(400).json({ message: passwordError })
    }

    if (currentPassword === newPassword) {
        return res.status(400).json({ message: 'New password must be different from your current password' })
    }

    try {
        const user = await prisma.user.findUnique({ where: { id: req.user!.id } })
        if (!user) {
            return res.status(404).json({ message: 'User not found' })
        }

        const isMatch = await bcrypt.compare(currentPassword, user.password)
        if (!isMatch) {
            return res.status(400).json({ message: 'Current password is incorrect' })
        }

        const hashedPassword = await bcrypt.hash(newPassword, 10)
        await prisma.user.update({
            where: { id: user.id },
            data: { password: hashedPassword }
        })

        return res.status(200).json({ message: 'Password updated successfully' })
    } catch (error) {
        return res.status(500).json({ message: 'Internal Server Error' })
    }
})

router.put('/username', authMiddleware, async (req: AuthRequest, res: Response) => {
    const { username } = req.body

    if (!username || typeof username !== 'string' || username.trim().length === 0) {
        return res.status(400).json({ message: 'Username is required' })
    }

    if (username.trim().length > 30) {
        return res.status(400).json({ message: 'Username must be 30 characters or less' })
    }

    try {
        const updated = await prisma.user.update({
            where: { id: req.user!.id },
            data: { username: username.trim() },
            select: { id: true, email: true, username: true }
        })
        return res.json(updated)
    } catch (error) {
        console.error('Error updating username:', error)
        return res.status(500).json({ message: 'Internal Server Error' })
    }
})

router.get('/me', authMiddleware, async (req: AuthRequest, res: Response) => {
    try {
        const user = await prisma.user.findUnique({
            where: { id: req.user!.id },
            select: { id: true, email: true, username: true }
        })
        if (!user) {
            return res.status(404).json({ message: 'User not found' })
        }
        return res.json(user)
    } catch (error) {
        return res.status(500).json({ message: 'Internal Server Error' })
    }
})

export default router