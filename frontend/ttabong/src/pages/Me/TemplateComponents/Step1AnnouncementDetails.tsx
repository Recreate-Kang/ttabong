import React from "react";
import { Textarea } from "@/components/ui/textarea";
import { Input } from "@/components/ui/input";
import { TemplateFormData } from "@/types/template";

interface Step1Props {
  templateData: {
    title: string;
    description: string;
    images: string[];
  };
  setTemplateData: React.Dispatch<React.SetStateAction<TemplateFormData>>;
}

const Step1AnnouncementDetails: React.FC<Step1Props> = ({ templateData, setTemplateData }) => {
  const fileToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = error => reject(error);
    });
  };

  const handleImageUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      const newImages = Array.from(event.target.files).slice(0, 10 - templateData.images.length);
      const base64Images = await Promise.all(newImages.map(fileToBase64));
      
      setTemplateData(prev => ({
        ...prev,
        images: [...prev.images, ...base64Images]
      }));
    }
  };

  const removeImage = (index: number) => {
    setTemplateData(prev => ({
      ...prev,
      images: prev.images.filter((_, i) => i !== index)
    }));
  };

  return (
    <div className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">공고 제목</label>
        <Input
          type="text"
          placeholder="[행복한 가게] 오후시간 도서 녹음을 위한 봉사자 모집"
          value={templateData.title}
          onChange={(e) => setTemplateData(prev => ({
            ...prev,
            title: e.target.value
          }))}
          className="w-full"
        />
      </div>
      
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">공고 내용</label>
        <Textarea
          placeholder="상세한 공고 내용을 입력해주세요."
          value={templateData.description}
          onChange={(e) => setTemplateData(prev => ({
            ...prev,
            description: e.target.value
          }))}
          className="min-h-[150px]"
        />
      </div>
      
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          사진 추가 ({templateData.images.length}/10)
        </label>
        <div className="flex gap-2 items-center">
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={handleImageUpload}
            className="hidden"
            id="image-upload"
            disabled={templateData.images.length >= 10}
          />
          <label
            htmlFor="image-upload"
            className={`w-20 h-20 border flex items-center justify-center cursor-pointer
              ${templateData.images.length >= 10 ? 'opacity-50 cursor-not-allowed' : 'hover:bg-gray-50'}`}
          >
            📷
          </label>
        </div>
        
        <div className="grid grid-cols-5 gap-2 mt-2">
          {templateData.images.map((image, index) => (
            <div key={index} className="relative w-20 h-20 border rounded-md overflow-hidden">
              <img
                src={image}
                alt={`upload-${index}`}
                className="w-full h-full object-cover"
              />
              <button
                type="button"
                onClick={() => removeImage(index)}
                className="absolute top-0 right-0 bg-red-500 text-white text-xs p-1 rounded-bl-md"
              >
                ✕
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Step1AnnouncementDetails;
