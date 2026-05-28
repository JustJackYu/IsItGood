import { Router, Request, Response } from 'express'
import prisma from '../prisma/client'
import bcrypt from 'bcrypt'
import jwt from 'jsonwebtoken'

const router = Router()

router.post('/register', async (req: Request, res: Response) => {
    const { email, password } = req.body

    try {
        const existingUser = await prisma.user.findUnique({ where: { email } })
        if (existingUser) {
            return res.status(400).json({ message: 'Email already in use' })
        }

        const hashedPassword = await bcrypt.hash(password, 10)

        const newUser = await prisma.user.create({
            data: {
                email,
                password: hashedPassword
            }
        })

        const token = jwt.sign(
            { id: newUser.id, email: newUser.email },
            process.env.JWT_SECRET!,
            { expiresIn: '7d' }
        )

        res.status(200).json({ token })
    }
    catch (error) {
        return res.status(500).json({ message: 'Internal Server Error' })
    }
})

router.post('/login', async (req: Request, res: Response) => {
    const { email, password } = req.body

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
            { expiresIn: '7d' }
        )

        res.status(200).json({ token })
    }
    catch (error) {
        return res.status(500).json({ message: 'Internal Server Error' })
    }
})

export default router