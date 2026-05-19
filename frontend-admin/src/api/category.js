import request from './request'

export function getCategoryList() {
  return request.get('/categories')
}

export function createCategory(name, voucherType) {
  return request.post('/categories', { name, voucherType })
}

export function updateCategory(id, name, voucherType) {
  return request.put(`/categories/${id}`, { name, voucherType })
}

export function deleteCategory(id) {
  return request.delete(`/categories/${id}`)
}
