// API 响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页响应
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

// 用户
export interface User {
  id: number
  username: string
  email?: string
  nickname?: string
  avatar?: string
  balance?: number
  role?: string
}

// 登录响应
export interface LoginResponse {
  token: string
  userInfo: User
}

// 游戏
export interface Game {
  id: number
  title: string
  description?: string
  detailDescription?: string
  coverImage?: string
  bannerImage?: string
  screenshots?: string
  videoUrl?: string
  originalPrice: number
  discountPrice?: number
  discountPercent?: number
  developer?: string
  publisher?: string
  releaseDate?: string
  minRequirements?: string
  recRequirements?: string
  tags?: string
  stock?: number
  salesCount?: number
  rating?: number
  ratingCount?: number
  isFeatured?: number
}

// 分类
export interface Category {
  id: number
  name: string
  description?: string
  icon?: string
  sortOrder?: number
}

// 购物车项
export interface CartItem {
  id: number
  userId: number
  gameId: number
  quantity: number
  game: Game
}

// 愿望单项
export interface WishlistItem {
  id: number
  userId: number
  gameId: number
  addedPrice?: number
  addedOriginalPrice?: number
  createdAt: string
  game: Game
  priceDrop?: number
  priceDropPercent?: number
  lowestPrice?: number
  currentPrice?: number
}

// 价格历史
export interface PriceHistory {
  id: number
  gameId: number
  originalPrice: number
  discountPrice?: number
  newDiscountPrice?: number
  discountPercent: number
  priceChange?: number
  changePercent?: number
  createdAt: string
  game?: Game
}

// 价格图表数据点
export interface PricePoint {
  date: string
  price: number
  dateLabel: string
}

// 价格图表DTO
export interface PriceChartDTO {
  pricePoints: PricePoint[]
  currentPrice: number
  originalPrice: number
  lowestPrice: number
  highestPrice: number
  averagePrice: number
}

// 通知
export interface Notification {
  id: number
  userId: number
  type: string
  gameId?: number
  title: string
  content?: string
  priceDrop?: number
  priceDropPercent?: number
  oldPrice?: number
  newPrice?: number
  isRead: number
  readAt?: string
  createdAt: string
  game?: Game
}

// 订单
export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  payAmount: number
  discountAmount?: number
  status: 'PENDING' | 'PAID' | 'CANCELLED' | 'COMPLETED'
  payTime?: string
  createdAt: string
  orderItems?: OrderItem[]
}

// 订单项
export interface OrderItem {
  id: number
  orderId: number
  gameId: number
  gameTitle: string
  gameCover?: string
  price: number
  quantity: number
}

// 用户游戏库
export interface UserLibrary {
  id: number
  userId: number
  gameId: number
  orderId?: number
  playTime?: number
  lastPlayedAt?: string
  createdAt: string
  game: Game
}

// 评论
export interface GameReview {
  id: number
  userId: number
  gameId: number
  rating: number
  content?: string
  isRecommend?: number
  helpfulCount?: number
  createdAt: string
  user?: User
}

// 游戏查询参数
export interface GameQueryParams {
  keyword?: string
  categoryId?: number
  priceRange?: string
  sortBy?: string
  sortOrder?: string
  page?: number
  size?: number
  onSale?: boolean
  featured?: boolean
}

// 秒杀活动状态
export type FlashSaleStatus = 'NOT_STARTED' | 'ONGOING' | 'SOLD_OUT' | 'ENDED'

// 秒杀活动
export interface FlashSale {
  id: number
  gameId: number
  flashPrice: number
  stockCount: number
  soldCount: number
  startTime: string
  endTime: string
  perUserLimit: number
  status: number
  createdAt?: string
  updatedAt?: string
  game: Game
  activityStatus: FlashSaleStatus
}

// 秒杀下单响应
export interface FlashSalePurchaseStatus {
  purchasedCount: number
  isLoggedIn: boolean
}

// 积分账户
export interface UserPoints {
  id: number
  userId: number
  balance: number
  totalEarned: number
  totalSpent: number
  consecutiveDays: number
  lastSignDate: string
  createdAt: string
  updatedAt: string
}

// 积分流水
export interface PointLog {
  id: number
  userId: number
  type: 'EARN' | 'SPEND'
  amount: number
  balanceAfter: number
  source: string
  sourceId: string
  description: string
  createdAt: string
}

// 积分商品
export interface PointProduct {
  id: number
  name: string
  description: string
  image: string
  pointsRequired: number
  stock: number
  soldCount: number
  type: 'COUPON' | 'VIRTUAL'
  value: number
  status: number
  sortOrder: number
  createdAt: string
  updatedAt: string
}

// 兑换记录
export interface ExchangeRecord {
  id: number
  userId: number
  productId: number
  productName: string
  productImage: string
  pointsSpent: number
  status: 'PENDING' | 'COMPLETED' | 'FAILED'
  redeemCode: string
  createdAt: string
  product?: PointProduct
}

// 签到结果
export interface SignInResult {
  signedToday: boolean
  consecutiveDays: number
  pointsEarned: number
  bonusPoints: number
  totalPoints: number
  message: string
}

// 兑换结果
export interface ExchangeResult {
  recordId: number
  productName: string
  productImage: string
  pointsSpent: number
  remainingPoints: number
  redeemCode: string
  message: string
}

// 充值套餐
export interface RechargePlan {
  id: number
  name: string
  amount: number
  bonusAmount: number
  description: string
  icon?: string
  tag?: string
  tagColor?: string
  sortOrder: number
  status: number
  createdAt: string
  updatedAt: string
}

// 充值订单
export interface RechargeOrder {
  id: number
  orderNo: string
  userId: number
  planId: number
  planName: string
  amount: number
  bonusAmount: number
  totalAmount: number
  payMethod: string
  status: 'PENDING' | 'PAID' | 'CANCELLED' | 'TIMEOUT'
  payTime?: string
  expireTime: string
  transactionId?: string
  remark?: string
  createdAt: string
  updatedAt: string
}

// 余额流水
export interface BalanceLog {
  id: number
  userId: number
  type: 'RECHARGE' | 'CONSUME' | 'REFUND'
  amount: number
  balanceAfter: number
  source: string
  sourceId: string
  description: string
  createdAt: string
}

// 开发商
export interface Developer {
  id: number
  name: string
  avatar?: string
  description?: string
  country?: string
  foundedYear?: number
  website?: string
  followerCount: number
  gameCount: number
  createdAt: string
  updatedAt: string
}

// 开发商详情（含游戏列表和关注状态）
export interface DeveloperDetail {
  developer: Developer
  games: Game[]
  isFollowing: boolean
}

// 开发商关注记录
export interface DeveloperFollowItem {
  id: number
  userId: number
  developerId: number
  createdAt: string
  developer: Developer
}
