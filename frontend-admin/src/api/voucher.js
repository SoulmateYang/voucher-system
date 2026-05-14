import request from './request'

export function verifyVoucher(voucherCode) {
  return request.post('/vouchers/lookup', { voucherCode })
}

export function confirmVerify(voucherCode) {
  return request.post('/vouchers/confirm', { voucherCode })
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
