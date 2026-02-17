import Link from "next/link";
import { Heading } from "@/components/atoms/heading";

export function Sidebar() {
  return (
    <aside className="w-20 h-screen bg-white border-r border-gray-200 flex flex-col items-center py-6 gap-6">
      <Heading level={2} className="text-sm font-semibold">
        Notes
      </Heading>

      <Link
        href="/add"
        className="w-10 h-10 flex items-center justify-center rounded-full bg-blue-600 text-white hover:bg-blue-700 transition-colors cursor-pointer"
        aria-label="Add new note"
      >
        <svg
          className="w-5 h-5"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 4v16m8-8H4"
          />
        </svg>
      </Link>
    </aside>
  );
}
