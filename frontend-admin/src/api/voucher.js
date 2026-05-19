import request from './request'

export function verifyVoucher(voucherCode) {
  return request.post('/vouchers/lookup', { voucherCode })
}

export function confirmVerify(voucherCode, orderAmount) {
  return request.post('/vouchers/confirm', { voucherCode, orderAmount })
}

export function getTodayRecords(params) {
  return request.get('/vouchers/today-records', { params })
}

export function getVoucherDetail(voucherCode) {
  return request.get(`/vouchers/${voucherCode}`)
}

export function getReports(params) {
  return request.get('/reports/summary', { params })
}

export function getDailyTrend(params) {
  return request.get('/reports/daily-trend', { params })
}

export function getVoucherList(params) {
  return request.get('/vouchers', { params })
}

export function updateVoucher(id, data) {
  return request.put(`/vouchers/${id}`, data)
}

export function assignCategory(id, categoryId) {
  return request.put(`/vouchers/${id}/category`, { categoryId })
}

export function batchAssignCategory(voucherIds, categoryId) {
  return request.put('/vouchers/category/batch', { voucherIds, categoryId })
}

