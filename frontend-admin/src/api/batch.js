import request from './request'

export function getBatchList(params) {
  return request.get('/batches', { params })
}

export function createBatch(data) {
  return request.post('/batches', data)
}

export function getBatchDetail(id) {
  return request.get(`/batches/${id}`)
}

export function issueVouchers(data) {
  return request.post('/batches/issue', data)
}

export function getVouchersByBatch(batchId, params) {
  return request.get(`/batches/${batchId}/vouchers`, { params })
}
