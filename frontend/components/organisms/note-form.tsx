"use client";

import { useState } from "react";
import { FormField } from "@/components/molecules/form-field";
import { Button } from "@/components/atoms/button";
import { IconButton } from "@/components/atoms/icon-button";
import { Label } from "@/components/atoms/label";

interface NoteFormProps {
  onSubmit: (note: { content: string; color: string }) => void;
  initialContent?: string;
  initialColor?: string;
}

const COLORS = ["#FCD34D", "#FCA5A5", "#C4B5FD", "#67E8F9", "#D9F99D"];

export function NoteForm({
  onSubmit,
  initialContent = "",
  initialColor = "#FCA5A5",
}: NoteFormProps) {
  const [content, setContent] = useState(initialContent);
  const [selectedColor, setSelectedColor] = useState(initialColor);
  const isEditMode = Boolean(initialContent);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (content.trim()) {
      onSubmit({ content, color: selectedColor });
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="flex flex-col gap-4"
      aria-label="Add new note"
    >
      <FormField
        id="note-content"
        label="Note"
        value={content}
        onChange={setContent}
        placeholder="Enter note content"
        type="textarea"
        rows={4}
        required
      />

      <fieldset>
        <Label>Color</Label>
        <div
          className="flex gap-3 mt-2"
          role="group"
          aria-label="Select note color"
        >
          {COLORS.map((color, index) => (
            <IconButton
              key={color}
              onClick={() => setSelectedColor(color)}
              ariaLabel={`Select color ${index + 1}`}
              variant="color"
              color={color}
            >
              {selectedColor === color && (
                <svg
                  className="w-4 h-4 text-white"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                    clipRule="evenodd"
                  />
                </svg>
              )}
              <span className="sr-only">Color {index + 1}</span>
            </IconButton>
          ))}
        </div>
      </fieldset>

      <Button type="submit" variant="primary">
        {isEditMode ? "Update Note" : "Add Note"}
      </Button>
    </form>
  );
}
