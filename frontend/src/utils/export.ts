import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'

export const exportRowsToExcel = (rows: Record<string, unknown>[], fileName: string, sheetName = 'Sheet1') => {
  const worksheet = XLSX.utils.json_to_sheet(rows)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)
  const output = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
  saveAs(new Blob([output], { type: 'application/octet-stream' }), fileName)
}
