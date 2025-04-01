// email_EmailContent.js
export default function EmailContent({ content }) {
    return (
      <div className="p-4 bg-gray-200 rounded-lg h-full overflow-y-auto">
        {content ? (
          <p className="text-gray-700 whitespace-pre-wrap">{content}</p>
        ) : (
          <p className="text-gray-500 italic">No email content available.</p>
        )}
      </div>
    );
  }
  