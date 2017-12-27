AppTitle("Spiel")
screenX = 1680	
screenY = 1050
Graphics screenX, screenY, 8, 2   

;HidePointer()
Global playerWait  = LoadImage("playerWait.bmp")  
Global playerJumpingRight = LoadImage("playerJumpingRight.bmp")
Global playerJumpingLeft = LoadImage("playerJumpingLeft.bmp")
Global playerWalkLeft0 = LoadImage("playerWalkLeft0.bmp")
Global playerWalkLeft1 = LoadImage("playerWalkLeft1.bmp")
Global playerWalkRight0 = LoadImage("playerWalkRight0.bmp")
Global playerWalkRight1 = LoadImage("playerWalkRight1.bmp")
Global playerWidth = 32
Global tileWidth = 32

;drawing at lower left corner
HandleImage playerWait, 0, playerwidth
HandleImage playerJumpingRight, 0, playerWidth
HandleImage playerJumpingLeft, 0, playerWidth
HandleImage playerWalkLeft0, 0, playerWidth
HandleImage playerWalkLeft1, 0, playerWidth
HandleImage playerWalkRight0, 0, playerWidth
HandleImage playerWalkRight1, 0, playerWidth




; update the game content every 1 / "timer" hertz
Global timer   = CreateTimer(35)  

;1 - right, -1 - left
Global direction = 1

Dim imageArray(128)
;0-32 are reserved for background
;
imageArray(33) = LoadImage("floor0.bmp")
imageArray(34) = LoadImage("floor1.bmp")
imageArray(35) = LoadImage("floor2.bmp")

Global DEBUG = False

Global nextFloor = 0

Global ArrayUpperBoundX = 54
Global ArrayUpperBoundY = 30
; arrays are always global
Dim tileArray(ArrayUpperBoundX, ArrayUpperBoundY)

ReadLevelData()

For i = 0 To ArrayUpperBoundX
	tileArray(i,ArrayUpperBoundY-4) = 33
Next
For i = 0 To ArrayUpperBoundX
	tileArray(i,ArrayUpperBoundY-3) = 34
Next
For i = 0 To ArrayUpperBoundX
	tileArray(i,ArrayUpperBoundY-2) = 34
Next
tileArray(1,25) = 33
tileArray(8,22) = 33
tileArray(15,20) = 33
tileArray(20,10) = 33
tileArray(30,27) = 33
For i = 4 To 20
	tileArray(i,i) = 33
Next
For i = 4 To 20
	tileArray(30-i,i) = 33
Next
For i = 20 To 30
	tileArray(36, i) = 33
Next

tileArray(20,27) = 0
tileArray(21,27) = 0
tileArray(20,26) = 0
tileArray(21,26) = 0

;Player Pos
Global px = 128	
Global py = 128

; current player speed
Global vx = 0
Global vy = 0


;JumpTime becomes greater by JumTimeInterval if the player is jumping 
Global jumpTime# = 0.0 

;true if Player in jumping State
Global jumping = False 

Const jumpTimeInterval# = 0.005
Const gravity = 10

;delta x
Const dx = 10
Const dy = 10

Const maxVy = 20

SetBuffer BackBuffer()

; game main loop

; DRAWING MUST BE AFTER CLS
Repeat 

	If KeyDown(205) Then ; rechts
		If (tileArray(mapToTile(px)+2, mapToTile(py)-1) <> 0 And mapToWorld(maptotile(px)+2)-(px+tileWidth) < dx) Or (jumping And (tileArray(maptotile(px)+2, mapToTile(py)) And mapToWorld(maptotile(px)+2)-(px+tileWidth) < dx)) Then
			px = mapToWorld(maptotile(px)+2)-tileWidth-1
		Else
			px = px +dx
		EndIf
		direction = 1
	EndIf 
	
	If KeyDown(203) Then ; links
		If (tileArray(mapToTile(px)-1, mapToTile(py)-1) <> 0 And Abs((mapToWorld(maptotile(px)-1)+tileWidth)-px) < dx) Or (jumping And (tileArray(maptotile(px)-1, mapToTile(py)) And Abs((mapToWorld(maptotile(px)-1)+tileWidth)-px) < dx)) Then
			px = mapToWorld(maptotile(px)-1)+tileWidth
		Else
			px = px -dx
		EndIf
		direction = -1
	EndIf 
	
	If KeyHit(200) Then ; oben
		If Not jumping Then 
			vy = -dy
			jumping = True
		EndIf
	EndIf	
	
	;keep player vy below maxVy
	If jumping Then 
		If vy < maxVy Then
			vy = vy + gravity * jumpTime#	
			advanceJumpTime()
		Else
			vy = maxVy
		EndIf
	EndIf
	
	; collides with any rect
	;tilearray returns a value <> 0 if there is a block at the given position
	If jumping And (  tileArray(mapToTile(px), mapToTile(py)+1) <> 0 Or tileArray(mapToTile(px)+1, mapToTile(py)+1) <> 0  ) And vy > 0 
		y = mapToWorld(mapToTile(py)+1)  ;position on the block 
		If py+vy+1 > y Then
			py = y             ; let player stop on the block
			stopJumping()
		EndIf
		
	; fall down		
	ElseIf jumping = False And 	tileArray(mapToTile(px)+1, mapToTile(py)) = 0 And tileArray(mapToTile(px), mapToTile(py)) = 0 
		jumping = True  
		vy = 1 ; fall faster				
	EndIf 
	
	; move the player by vy
	py = py + vy

	; NO DRAWING BEFORE HERE
	Cls
	WaitTimer timer
	
	If DEBUG Then
	debugDraw()
	EndIf
	drawWorld()
	drawPlayer()
	Flip 
		
Until KeyHit(1) ;ImagesCollide(player, px, py, frame1, gegner, gx, gy, frame2)     

End



Function advanceJumpTime()
jumpTime# = jumpTime# + jumpTimeInterval#
End Function 


Function drawWorld()
Color 0, 255, 0
;Draw Array content, 0 is background, 1 is wall
For a = 0 To ArrayUpperBoundX	
	For b = 0 To ArrayUpperBoundY
		If tileArray(a,b) <> 0 Then 
			;drawTileImage(imageArray(tileArray(a,b)),a,b)
			drawTileImage(imageArray(35),a,b)
		EndIf
	Next
Next
End Function


; checks if the point (px,py) is inside the rectangle at pos (rx,ry) with the size width and height
Function PointInRect(px, py, rx, ry, width, height)
Return rx <= px And px <= rx + width And ry <= py And py <= ry + height
End Function


Function stopJumping()
vy = 0
jumpTime# = 0.0
jumping = False
End Function


Function mapToTile(point)
Return point  / tileWidth
End Function 

Function mapToWorld(point)
Return point * tilewidth
End Function                           

;Draws rect at tx, ty in tile-coordinates
Function drawTileRect(tx,ty)
Rect mapToWorld(tx), mapToWorld(ty), tileWidth, tileWidth
End Function 

Function drawTileImage(image,tx,ty)
DrawImage image, mapToWorld(tx), mapToWorld(ty)
End Function

Function drawPlayer()
If jumping And direction > 0 Then
	DrawImage playerJumpingRight, px, py
ElseIf jumping And direction < 0 Then
	DrawImage playerJumpingLeft, px, py
ElseIf direction > 0 Then
	DrawImage playerWalkRight0, px, py
ElseIf direction < 0 Then
	DrawImage playerWalkLeft0, px, py
Else 
	DrawImage playerWait, px, py
EndIf
End Function 

Function ReadLevelData()
filein = ReadFile("lvl1.txt")
arrayIndexY = 0
arrayIndexX = 0
While Not Eof(filein) And arrayIndexY < ArrayUpperBoundY
	myLine$ = ReadLine(filein)
	For i = 0 To Len(myLine$)-1
		c$ = Mid(myLine$,i+1,1)
		
		If c$ <> " " Then 
		;Print arrayIndexX
			If arrayIndexX <= ArrayUpperBoundX And arrayIndexY <= ArrayUpperBoundY Then 
				;Print arrayIndexY
				;Print arrayIndexX
				tileArray(arrayIndexX,arrayIndexY) = c
				arrayIndexX = arrayIndexX +1
			EndIf
		EndIf
		
	Next 
	arrayIndexY = arrayIndexY +1 
	arrayIndexX = 0
Wend 
End Function 

;UNUSED
Function willCollideWithRect(rx,ry,width,height)
Return PointInRect(px, py + dy, rx, ry, width,height) Or PointInRect(px+ playerWidth, py + dy, rx, ry, width,height)
End Function

Function contactWithRect(rx,ry,width,height)
Return PointInRect(px, py + 2, rx, ry, width,height) Or PointInRect(px+ playerWidth, py + 2, rx, ry, width,height)
End Function 

;draws collision rects and player coordinates
Function debugDraw()
Color 255, 0, 0
drawTileRect(mapToTile(px), mapToTile(py))
drawTileRect(mapToTile(px)+1, mapToTile(py))
Color 0, 0, 255
drawTileRect(mapToTile(px), mapToTile(py)+1)	
drawTileRect(mapToTile(px)+1, mapToTile(py)+1)
Text 0, 0, mapToTile(px) + " " + mapToTile(py)
drawTileRect(maptotile(px)-1, mapToTile(py)-1)
drawTileRect(maptotile(px)+2, mapToTile(py)-1)
Color 255, 255, 255
Rect mapToWorld(maptotile(px)-1) ,mapToWorld(mapToTile(py)-1) , 2, 2
End Function 






















                                                                                                                                                                                                                                                                                                                                                                                                                                                                   