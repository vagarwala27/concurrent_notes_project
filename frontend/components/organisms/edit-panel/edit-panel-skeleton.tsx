export function EditPanelSkeleton() {
  return (
    <aside className="w-96 sticky top-0 h-fit">
      <div className="flex justify-between items-center mb-4">
        <div className="h-6 w-24 bg-gray-200 rounded animate-pulse" />
        <div className="h-10 w-10 bg-gray-200 rounded-full animate-pulse" />
      </div>
      <section className="bg-white p-6 rounded-lg shadow-sm">
        <div className="space-y-4">
          <div className="h-32 bg-gray-200 rounded animate-pulse" />
          <div className="h-10 bg-gray-200 rounded animate-pulse" />
          <div className="h-10 w-32 bg-gray-200 rounded animate-pulse" />
        </div>
      </section>
    </aside>
  );
}
