"use client"

import type React from "react"
import { useState, useRef, useCallback } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Slider } from "@/components/ui/slider"
import { RotateCw, ZoomIn, ZoomOut } from "lucide-react"

interface ImageCropperProps {
  isOpen: boolean
  onClose: () => void
  onCrop: (croppedFile: File) => void
  imageFile: File | null
  aspectRatio?: number
}

export function ImageCropper({ isOpen, onClose, onCrop, imageFile, aspectRatio = 1 }: ImageCropperProps) {
  const [scale, setScale] = useState(1)
  const [rotation, setRotation] = useState(0)
  const [position, setPosition] = useState({ x: 0, y: 0 })
  const [isDragging, setIsDragging] = useState(false)
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 })
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const imageRef = useRef<HTMLImageElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)

  const handleMouseDown = useCallback(
    (e: React.MouseEvent) => {
      setIsDragging(true)
      setDragStart({ x: e.clientX - position.x, y: e.clientY - position.y })
    },
    [position],
  )

  const handleMouseMove = useCallback(
    (e: React.MouseEvent) => {
      if (!isDragging) return
      setPosition({
        x: e.clientX - dragStart.x,
        y: e.clientY - dragStart.y,
      })
    },
    [isDragging, dragStart],
  )

  const handleMouseUp = useCallback(() => {
    setIsDragging(false)
  }, [])

  const handleCrop = useCallback(async () => {
    if (!imageFile || !canvasRef.current || !imageRef.current || !containerRef.current) return

    const canvas = canvasRef.current
    const ctx = canvas.getContext("2d")
    if (!ctx) return

    const img = imageRef.current
    const cropPreviewSize = 200 // Kích thước khung crop trong preview
    const cropSize = 300 // Kích thước output cuối
    const scaleFactor = cropSize / cropPreviewSize

    const containerW = containerRef.current.clientWidth
    const containerH = containerRef.current.clientHeight
    const naturalW = img.naturalWidth
    const naturalH = img.naturalHeight
    const fitScale = Math.min(containerW / naturalW, containerH / naturalH)
    const fitW = naturalW * fitScale
    const fitH = naturalH * fitScale

    canvas.width = cropSize
    canvas.height = cropSize

    // Clear canvas
    ctx.clearRect(0, 0, cropSize, cropSize)

    // Save context
    ctx.save()

    ctx.scale(scaleFactor, scaleFactor)
    ctx.translate(cropPreviewSize / 2, cropPreviewSize / 2)
    ctx.translate(position.x, position.y)
    ctx.scale(scale, scale)
    ctx.rotate((rotation * Math.PI) / 180)
    ctx.drawImage(img, -fitW / 2, -fitH / 2, fitW, fitH)

    // Restore context
    ctx.restore()

    // Convert canvas to blob
    canvas.toBlob(
      (blob) => {
        if (blob) {
          const croppedFile = new File([blob], `cropped-${imageFile.name}`, {
            type: "image/jpeg",
            lastModified: Date.now(),
          })
          onCrop(croppedFile)
        }
      },
      "image/jpeg",
      0.9,
    )
  }, [imageFile, scale, rotation, position, onCrop])

  if (!imageFile) return null

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Crop Avatar Image</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          {/* Preview Area */}
          <div
            ref={containerRef}
            className="relative w-full h-80 bg-gray-100 rounded-lg overflow-hidden cursor-move"
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            onMouseLeave={handleMouseUp}
          >
            <img
              ref={imageRef}
              src={URL.createObjectURL(imageFile) || "/placeholder.svg"}
              alt="Crop preview"
              className="absolute inset-0 w-full h-full object-contain pointer-events-none"
              style={{
                transform: `translate(${position.x}px, ${position.y}px) scale(${scale}) rotate(${rotation}deg)`,
                transformOrigin: "center",
              }}
            />

            {/* Crop overlay */}
            <div className="absolute inset-0 pointer-events-none">
              <div className="absolute inset-0 bg-black bg-opacity-50"></div>
              <div
                className="absolute bg-transparent border-2 border-white shadow-lg"
                style={{
                  width: "200px",
                  height: "200px",
                  left: "50%",
                  top: "50%",
                  transform: "translate(-50%, -50%)",
                  borderRadius: "50%",
                }}
              ></div>
            </div>
          </div>

          {/* Controls */}
          <div className="space-y-4">
            <div className="flex items-center gap-4">
              <ZoomOut className="w-4 h-4" />
              <Slider
                value={[scale]}
                onValueChange={(value) => setScale(value[0])}
                min={0.5}
                max={3}
                step={0.1}
                className="flex-1"
              />
              <ZoomIn className="w-4 h-4" />
              <span className="text-sm text-gray-600 min-w-12">{Math.round(scale * 100)}%</span>
            </div>

            <div className="flex items-center gap-4">
              <Button variant="outline" size="sm" onClick={() => setRotation((prev) => prev - 90)}>
                <RotateCw className="w-4 h-4 mr-2 transform scale-x-[-1]" />
                Rotate Left
              </Button>
              <Button variant="outline" size="sm" onClick={() => setRotation((prev) => prev + 90)}>
                <RotateCw className="w-4 h-4 mr-2" />
                Rotate Right
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setScale(1)
                  setRotation(0)
                  setPosition({ x: 0, y: 0 })
                }}
              >
                Reset
              </Button>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button onClick={handleCrop}>Crop & Upload</Button>
        </DialogFooter>

        {/* Hidden canvas for cropping */}
        <canvas ref={canvasRef} className="hidden" />
      </DialogContent>
    </Dialog>
  )
}