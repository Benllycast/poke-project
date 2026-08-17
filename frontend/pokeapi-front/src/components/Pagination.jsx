function Pagination({ page, totalPages, onPageChange }) {
  return (
    <div className="pagination">
      <button type="button" disabled={page <= 0} onClick={() => onPageChange(page - 1)}>
        Previous
      </button>
      <span>
        Page {page + 1} of {Math.max(totalPages, 1)}
      </span>
      <button type="button" disabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>
        Next
      </button>
    </div>
  )
}

export default Pagination
