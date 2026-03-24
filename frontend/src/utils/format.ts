export const formatCurrency = (value: number | string | null | undefined): string => {
  const amount = Number(value || 0)
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(amount)
}

export const formatNumber = (value: number | string | null | undefined): string => new Intl.NumberFormat('zh-CN').format(Number(value || 0))

export const formatDateTime = (value?: string | null): string => {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

export const statusTypeMap: Record<string, string> = {
  CREATED: 'info',
  RECEIVED: 'warning',
  INBOUND: 'success',
  PAID: 'success',
  ENABLED: 'success',
  DISABLED: 'danger',
  PENDING: 'warning',
  CHECK: 'primary',
  TRANSFER: 'warning',
  OUTBOUND: 'danger'
}

export const statusLabelMap: Record<string, string> = {
  CREATED: '已创建',
  RECEIVED: '已到货',
  INBOUND: '已入库',
  PAID: '已回款',
  ENABLED: '启用',
  DISABLED: '停用',
  PENDING: '待处理',
  CHECK: '盘点',
  TRANSFER: '调拨',
  PURCHASE: '采购',
  SALE: '销售',
  OUTBOUND: '出库'
}
