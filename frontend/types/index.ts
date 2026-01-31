// User
export type UserRole = 'USER' | 'ADMIN'

export interface User {
  id: number
  email: string
  name: string
  role: UserRole
}

// Auth
export interface LoginRequest {
  email: string
  password: string
}

export interface SignupRequest {
  email: string
  password: string
  name: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

// Event
export type EventStatus = 'DRAFT' | 'SCHEDULED' | 'OPEN' | 'CLOSED' | 'CANCELLED'

export interface EventSettings {
  useQueue: boolean
  queueBatchSize: number
  requirePayment: boolean
  maxReservationsPerUser: number
  reservationTimeLimit: number // seconds
}

export interface Event {
  id: number
  creatorId: number
  creatorName: string
  title: string
  description: string
  openAt: string
  closeAt: string
  maxParticipants: number
  currentParticipants: number
  status: EventStatus
  settings: EventSettings
  createdAt: string
}

export interface CreateEventRequest {
  title: string
  description: string
  openAt: string
  closeAt: string
  maxParticipants: number
  settings: EventSettings
}

export interface UpdateEventRequest extends Partial<CreateEventRequest> {}

// Slot
export interface Slot {
  id: number
  eventId: number
  name: string
  quantity: number
  availableQuantity: number
  price: number
}

export interface CreateSlotRequest {
  name: string
  quantity: number
  price: number
}

// Queue
export type QueueStatus = 'WAITING' | 'READY' | 'EXPIRED'

export interface QueueEntry {
  eventId: number
  position: number
  totalInQueue: number
  estimatedWaitTime: number // seconds
  status: QueueStatus
}

// Reservation
export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED'

export interface Reservation {
  id: number
  eventId: number
  eventTitle: string
  userId: number
  slotId?: number
  slotName?: string
  status: ReservationStatus
  createdAt: string
  confirmedAt?: string
  expiresAt?: string
}

export interface CreateReservationRequest {
  eventId: number
  slotId?: number
}

// Payment
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED'
export type PaymentMethod = 'CARD' | 'BANK_TRANSFER' | 'VIRTUAL_ACCOUNT'

export interface Payment {
  id: number
  reservationId: number
  amount: number
  method: PaymentMethod
  status: PaymentStatus
  paidAt?: string
}

export interface PaymentRequest {
  reservationId: number
  method: PaymentMethod
}

// API Response
export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

// Participant (for event creator view)
export interface Participant {
  userId: number
  userName: string
  userEmail: string
  reservationId: number
  slotName?: string
  status: ReservationStatus
  reservedAt: string
}
