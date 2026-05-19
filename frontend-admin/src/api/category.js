import request from './request'

export function getCategoryList() {
  return request.get('/categories')
}

export function createCategory(name) {
  return request.post('/categories', { name })
}

export function updateCategory(id, name) {
  return request.put(`/categories/${id}`, { name })
}

export function deleteCategory(id) {
  return request.delete(`/categories/${id}`)
}
