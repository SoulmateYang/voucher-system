import request from './request'

export function getConsumptionHistory(voucherId) {
  return request.get(`/vouchers/${voucherId}/consumptions`)
}
