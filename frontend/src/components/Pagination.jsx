/**
 * Reusable pagination bar. Takes a PagedResponse-shaped object and an onPage(n)
 * callback. Renders "Prev | Page X of Y | Next" with the right buttons disabled.
 */
export default function Pagination({ data, onPage }) {
  if (!data || data.total_pages <= 1) return null

  return (
    <div className="pagination">
      <button
        className="btn-link"
        disabled={data.first}
        onClick={() => onPage(data.page - 1)}
      >
        ← Prev
      </button>
      <span className="muted">
        Page {data.page + 1} of {data.total_pages} · {data.total_elements} total
      </span>
      <button
        className="btn-link"
        disabled={data.last}
        onClick={() => onPage(data.page + 1)}
      >
        Next →
      </button>
    </div>
  )
}
