

InitSprite()
UseJPEGImageDecoder()
UseJPEGImageEncoder()

LoadImage(0,"img_lr.jpg",0)    ; 0 ist Low Resolution Pic
LoadImage(1,"img_sr.jpg",0)    ; 1 ist NN Bild


Global tileSize = 336

Global tileSizeLow = tileSize / 4

Global BorderSize = 20

Structure tile
  x.i
  y.i
  RAverage.d
  GAverage.d
  BAverage.d
  RFactor.d
  GFactor.d
  BFactor.d
  RFactor2.d
  GFactor2.d
  BFactor2.d
  isSet.b
   R_left_average.d
   R_up_average.d
   R_right_average.d
   R_down_average.d
   G_left_average.d
   G_up_average.d
   G_right_average.d
   G_down_average.d
   B_left_average.d
   B_up_average.d
   B_right_average.d
   B_down_average.d
  size.i
EndStructure

Global NewMap tileMap.tile(512)
Global NewMap tileMapLow.tile(512)

Procedure generateTileMap(ImageID.i, Map tiles.tile(), tileS.i)
  height.i = ImageHeight(ImageID)
  width.i = ImageWidth(ImageID)
  sizeX = width/tileS
  sizeY = height/tileS
  ;Debug SizeX
  ;Debug SizeY
  
  For x = 0 To sizeX-1
    For y = 0 To sizeY-1 
      AddMapElement(tiles(),Str(x)+"|"+Str(y))
      tiles()\x = x
      tiles()\y = y
      tiles()\size = tileS
      tiles()\isSet = #False
    Next
  Next
  
EndProcedure


generateTileMap(0,tileMapLow(),tileSizeLow)
generateTileMap(1,tileMap(),tileSize)

Procedure setAveragesOnTiles(ImageID.i, Map tiles.tile())
  startingPixelX.i
  startingPixelY.i
  RSum.i = 0
  GSum.i = 0
  BSum.i = 0
  
  StartDrawing(ImageOutput(ImageID))
  ;Debug OutputWidth()
  ;Debug OutputHeight()
  
  ForEach tiles()
    SizeSquare = tiles()\size * tiles()\size
    startingPixelX = tiles()\x * tiles()\size
    startingPixelY = tiles()\y * tiles()\size
    ;Debug Str(tiles()\x)+"|"+Str(tiles()\y)
    For x = startingPixelX To startingPixelX+tiles()\size-1
      For y = startingPixelY  To startingPixelY+tiles()\size-1
        ;Debug Str(x)+"|"+Str(y)
        RSum + Red(Point(x,y))
        GSum + Green(Point(x,y))
        BSum + Blue(Point(x,y))
      Next
    Next
    
    tiles()\RAverage = RSum/SizeSquare
    tiles()\GAverage = GSum/SizeSquare
    tiles()\BAverage = BSum/SizeSquare
    RSum = 0
    GSum = 0
    BSum = 0
    
  Next
  
  StopDrawing()
  
EndProcedure

setAveragesOnTiles(0, tileMapLow())
setAveragesOnTiles(1, tileMap())


Procedure setFactors(Map MapRead.tile(), Map MapWrite.tile())
  RFactor.d = 0
  GFactor.d = 0
  BFactor.d = 0
  
  ForEach MapRead()
    *tile.tile = FindMapElement(MapWrite(),Str(MapRead()\x)+"|"+Str(MapRead()\y))
    If (*tile\RAverage <> 0)
      RFactor = MapRead()\RAverage / *tile\RAverage
    Else
      RFactor = MapRead()\RAverage
    EndIf
    If (*tile\GAverage <> 0)
      GFactor = MapRead()\GAverage / *tile\GAverage
    Else
      RFactor = MapRead()\GAverage
    EndIf
    If (*tile\BAverage <> 0)
      BFactor = MapRead()\BAverage / *tile\BAverage
    Else
      RFactor = MapRead()\BAverage
    EndIf
    *tile\RFactor = RFactor
    *tile\GFactor = GFactor
    *tile\BFactor = BFactor
  Next
EndProcedure

;

Procedure applyFactors(ImageID.i, Map FactorMap.tile())
  R.i
  G.i
  B.i
  StartDrawing(ImageOutput(ImageID))
  
  ForEach FactorMap()
    SizeSquare = FactorMap()\size * FactorMap()\size
    startingPixelX = FactorMap()\x * FactorMap()\size
    startingPixelY = FactorMap()\y * FactorMap()\size
    ;Debug Str(tiles()\x)+"|"+Str(tiles()\y)
    For x = startingPixelX To startingPixelX+FactorMap()\size-1
      For y = startingPixelY  To startingPixelY+FactorMap()\size-1
        ;Debug Str(x)+"|"+Str(y)
        R = Red(Point(x,y)) * FactorMap()\RFactor
        If R > 255
          R = 255
        EndIf
        
        G = Green(Point(x,y)) * FactorMap()\GFactor
        If G > 255
          G = 255
        EndIf
        
        B = Blue(Point(x,y)) * FactorMap()\BFactor
        If B > 255
          B = 255
        EndIf
        
        Plot(x,y,RGB(R,G,B))
      Next
    Next
    
  Next
  
  
  
  StopDrawing()
  
EndProcedure

Procedure applySingleFactor(ImageID.i, Map FactorMap.tile(),XEle.i,YEle.i)
  R.i
  G.i
  B.i
  StartDrawing(ImageOutput(ImageID))
  
  FindMapElement(FactorMap(),Str(XEle)+"|"+Str(YEle))
    SizeSquare = FactorMap()\size * FactorMap()\size
    startingPixelX = FactorMap()\x * FactorMap()\size
    startingPixelY = FactorMap()\y * FactorMap()\size
    ;Debug Str(tiles()\x)+"|"+Str(tiles()\y)
    For x = startingPixelX To startingPixelX+FactorMap()\size-1
      For y = startingPixelY  To startingPixelY+FactorMap()\size-1
        Factor1Size.d = 0
        Factor2Size.d = 0
        xNorm.d = (x-startingPixelX)/FactorMap()\size
        yNorm.d = (y-startingPixelY)/FactorMap()\size
        If xNorm = 0 And yNorm = 0
          Factor1Size = 0.5
          Factor2Size = 0.5
        Else
          Factor1Size = xNorm/(xNorm+yNorm)
          Factor2Size = yNorm/(xNorm+yNorm)
        EndIf
        ;Debug Str(x)+"|"+Str(y)
        R = Red(Point(x,y)) * (Factor1Size*FactorMap()\RFactor) + Red(Point(x,y)) * (Factor2Size*FactorMap()\RFactor2)
        If R > 255
          R = 255
        EndIf
        
        G = Green(Point(x,y)) * (Factor1Size*FactorMap()\GFactor) + Green(Point(x,y)) * (Factor2Size*FactorMap()\GFactor2)
        If G > 255
          G = 255
        EndIf
        
        B = Blue(Point(x,y)) * (Factor1Size*FactorMap()\BFactor) + Blue(Point(x,y)) * (Factor2Size*FactorMap()\BFactor2)
        If B > 255
          B = 255
        EndIf
        
        Plot(x,y,RGB(R,G,B))
      Next
    Next
  
  
  
  StopDrawing()
  
EndProcedure

;applyFactors(1,tileMap())

Procedure.i setRelativeFactors2(Map tiles.tile(),x.i,y.i,factor.d)
  xCount.i = 11
  yCount.i = 8
  
  If (x < 0 Or x > 11 Or y < 0 Or y > 8)
    ProcedureReturn 0
  EndIf
  *tile.tile = FindMapElement(tiles(),Str(x)+"|"+Str(y))
  If *tile\isSet = #True
    ProcedureReturn 0
  EndIf
  
   
  
    
  ;setRelativeFactors(tiles(),x+1,y+1)
  ;setRelativeFactors(tiles(),x-1,y+1)
  ;setRelativeFactors(tiles(),x-1,y-1)
  ;setRelativeFactors(tiles(),x+1,y-1)
  
  
EndProcedure

Procedure setRelativeFactors(Map tiles.tile(),x.i,y.i)
  RFactor.d = 0
  GFactor.d = 0
  BFactor.d = 0

  *tile.tile = FindMapElement(tiles(),Str(x)+"|"+Str(y))
  
  ForEach tiles()
    
    
    If (tiles()\RAverage <> 0)
      RFactor = *tile\RAverage / tiles()\RAverage
    Else
      RFactor = *tile\RAverage
    EndIf
    If (tiles()\GAverage <> 0)
      GFactor = *tile\GAverage / tiles()\GAverage
    Else
      RFactor = *tile\GAverage
    EndIf
    If (tiles()\BAverage <> 0)
      BFactor = *tile\BAverage /tiles()\BAverage
    Else
      RFactor = *tile\BAverage
    EndIf
    tiles()\RFactor = RFactor
    tiles()\GFactor = GFactor
    tiles()\BFactor = BFactor
  Next
EndProcedure

Procedure setSingleBorderAverage(ImageID.i, Map tiles.tile(), XEle.i, YEle.i)
  RSum.d = 0
  GSum.d = 0
  BSum.d = 0
  
  StartDrawing(ImageOutput(ImageID))
  
  FindMapElement(tiles(),Str(XEle)+"|"+Str(YEle))
    startingPixelX = tiles()\x * tiles()\size
    startingPixelY = tiles()\y * tiles()\size
    
    SizeBorderSquare = tiles()\size * BorderSize
    For x = startingPixelX To startingPixelX+BorderSize-1
      For y = startingPixelY  To startingPixelY+tiles()\size-1
        RSum + Red(Point(x,y))
        GSum + Green(Point(x,y))
        BSum + Blue(Point(x,y))
      Next
    Next
    tiles()\R_left_average = RSum/SizeBorderSquare
    tiles()\G_left_average = GSum/SizeBorderSquare
    tiles()\B_left_average = BSum/SizeBorderSquare
    ;Debug tiles()\R_left_average
    RSum = 0
    GSum = 0
    BSum = 0
    
    For x = startingPixelX To startingPixelX+tiles()\size-1
      For y = startingPixelY To startingPixelY+BorderSize -1
        ;Debug Str(x)+"|"+Str(y)
        RSum + Red(Point(x,y))
        GSum + Green(Point(x,y))
        BSum + Blue(Point(x,y))
      Next
    Next
    tiles()\R_up_average = RSum/SizeBorderSquare
    tiles()\G_up_average = GSum/SizeBorderSquare
    tiles()\B_up_average = BSum/SizeBorderSquare
    RSum = 0
    GSum = 0
    BSum = 0
    
    For x = startingPixelX+tiles()\size-BorderSize To startingPixelX+tiles()\size-1
      For y = startingPixelY To startingPixelY+tiles()\size -1
        ;Debug Str(x)+"|"+Str(y)
        RSum + Red(Point(x,y))
        GSum + Green(Point(x,y))
        BSum + Blue(Point(x,y))
      Next
    Next
    tiles()\R_right_average = RSum/SizeBorderSquare
    tiles()\G_right_average = GSum/SizeBorderSquare
    tiles()\B_right_average = BSum/SizeBorderSquare
    RSum = 0
    GSum = 0
    BSum = 0
    
    For x = startingPixelX To startingPixelX+tiles()\size-1
      For y = startingPixelY+tiles()\size-BorderSize To startingPixelY+tiles()\size -1
        ;Debug Str(x)+"|"+Str(y)
        RSum + Red(Point(x,y))
        GSum + Green(Point(x,y))
        BSum + Blue(Point(x,y))
      Next
    Next
    tiles()\R_down_average = RSum/SizeBorderSquare
    tiles()\G_down_average = GSum/SizeBorderSquare
    tiles()\B_down_average = BSum/SizeBorderSquare
    RSum = 0
    GSum = 0
    BSum = 0
  
  StopDrawing()
EndProcedure


Procedure setBroderAverages(ImageID.i, Map tiles.tile())
  RSum.d = 0
  GSum.d = 0
  BSum.d = 0
  
  StartDrawing(ImageOutput(ImageID))
  
  ForEach tiles()
    startingPixelX = tiles()\x * tiles()\size
    startingPixelY = tiles()\y * tiles()\size
    
    SizeBorderSquare = tiles()\size * BorderSize
    For x = startingPixelX To startingPixelX+BorderSize-1
      For y = startingPixelY  To startingPixelY+tiles()\size-1
        RSum + Red(Point(x,y))
        GSum + Green(Point(x,y))
        BSum + Blue(Point(x,y))
      Next
    Next
    tiles()\R_left_average = RSum/SizeBorderSquare
    tiles()\G_left_average = GSum/SizeBorderSquare
    tiles()\B_left_average = BSum/SizeBorderSquare
    ;Debug tiles()\R_left_average
    RSum = 0
    GSum = 0
    BSum = 0
    
    For x = startingPixelX To startingPixelX+tiles()\size-1
      For y = startingPixelY To startingPixelY+BorderSize -1
        ;Debug Str(x)+"|"+Str(y)
        RSum + Red(Point(x,y))
        GSum + Green(Point(x,y))
        BSum + Blue(Point(x,y))
      Next
    Next
    tiles()\R_up_average = RSum/SizeBorderSquare
    tiles()\G_up_average = GSum/SizeBorderSquare
    tiles()\B_up_average = BSum/SizeBorderSquare
    RSum = 0
    GSum = 0
    BSum = 0
    
    For x = startingPixelX+tiles()\size-BorderSize To startingPixelX+tiles()\size-1
      For y = startingPixelY To startingPixelY+tiles()\size -1
        ;Debug Str(x)+"|"+Str(y)
        RSum + Red(Point(x,y))
        GSum + Green(Point(x,y))
        BSum + Blue(Point(x,y))
      Next
    Next
    tiles()\R_right_average = RSum/SizeBorderSquare
    tiles()\G_right_average = GSum/SizeBorderSquare
    tiles()\B_right_average = BSum/SizeBorderSquare
    RSum = 0
    GSum = 0
    BSum = 0
    
    For x = startingPixelX To startingPixelX+tiles()\size-1
      For y = startingPixelY+tiles()\size-BorderSize To startingPixelY+tiles()\size -1
        ;Debug Str(x)+"|"+Str(y)
        RSum + Red(Point(x,y))
        GSum + Green(Point(x,y))
        BSum + Blue(Point(x,y))
      Next
    Next
    tiles()\R_down_average = RSum/SizeBorderSquare
    tiles()\G_down_average = GSum/SizeBorderSquare
    tiles()\B_down_average = BSum/SizeBorderSquare
    RSum = 0
    GSum = 0
    BSum = 0
  Next
  
  StopDrawing()
  
EndProcedure


Procedure overlayTiles(ImageID.i, Map tiles.tile())
  R.d = 0
  G.d = 0
  B.d = 0
  ROld.d
  GOld.d
  BOld.d
  RTransparency.d = 0
  GTransparency.d = 0
  BTransparency.d = 0

  
  StartDrawing(ImageOutput(ImageID))
  
  ForEach tiles()
    startingPixelX = tiles()\x * tiles()\size
    startingPixelY = tiles()\y * tiles()\size
    *this.tile = tiles()
    ;If (FindMapElement(tiles(),Str(tiles()\x+1)+"|"+Str(tiles()\y)) And FindMapElement(tiles(),Str(tiles()\x-1)+"|"+Str(tiles()\y)) And FindMapElement(tiles(),Str(tiles()\x)+"|"+Str(tiles()\y+1)) And FindMapElement(tiles(),Str(tiles()\x)+"|"+Str(tiles()\y-1)))
      *right.tile = FindMapElement(tiles(),Str(*this\x+1)+"|"+Str(*this\y))
      *left.tile = FindMapElement(tiles(),Str(*this\x-1)+"|"+Str(*this\y))
      *up.tile = FindMapElement(tiles(),Str(*this\x)+"|"+Str(*this\y-1))
      *down.tile = FindMapElement(tiles(),Str(*this\x)+"|"+Str(*this\y+1))
      FindMapElement(tiles(),Str(*this\x)+"|"+Str(*this\y))
      
      If *right <> #Null And *left <> #Null And *up <> #Null And *down <> #Null
        ;Debug "hey!"+ Str(tiles()\x)+ " | "+Str(tiles()\y)
      For x1 = startingPixelX To startingPixelX+tiles()\size-1
        For y1 = startingPixelY To startingPixelY+tiles()\size -1
          x.d = (x1-startingPixelX)/tiles()\size
          y.d = (y1-startingPixelY)/tiles()\size
          R = *left\R_right_average*(1-x)+*down\R_up_average*(1-y)+*up\R_down_average*y+*right\R_left_average*x
          G = *left\G_right_average*(1-x)+*down\G_up_average*(1-y)+*up\G_down_average*y+*right\G_left_average*x  
          B = *left\B_right_average*(1-x)+*down\B_up_average*(1-y)+*up\B_down_average*y+*right\B_left_average*x
          R/2
          G/2
          B/2
          If R > 255
            R = 255
          EndIf
          
          If G > 255
            G = 255
          EndIf
          
          If B > 255
            B = 255
          EndIf
          ROld = Red(Point(x1,y1))
          GOld = Green(Point(x1,y1))
          BOld = Blue(Point(x1,y1))
          
          RTransparency = 1-(0.5*Abs(0.5-x)+0.5*Abs(0.5-y))
          If RTransparency > 1
            RTransparency = 1
          EndIf
          RTransparency = 1-RTransparency
          GTransparency = 1-(0.5*Abs(0.5-x)+0.5*Abs(0.5-y))
          If GTransparency > 1
            GTransparency = 1
          EndIf
          GTransparency = 1-GTransparency
          BTransparency = 1-(0.5*Abs(0.5-x)+0.5*Abs(0.5-y))
          If BTransparency > 1
            BTransparency = 1
          EndIf
          BTransparency = 1-BTransparency
          
          Plot(x1,y1,RGB(R* RTransparency +ROld*(1-RTransparency),G * GTransparency+GOld*(1-GTransparency),B * BTransparency+BOld*(1-BTransparency)))
        Next
      Next
      
      
    EndIf
    *right = #Null
    *left = #Null
    *up = #Null
    *down = #Null
    
  Next
  
  StopDrawing()
  
EndProcedure

Procedure setBorderFactors(ImageID.i, Map tiles.tile())
  For i = 0 To 20
    For k = 0 To i
      *this.tile = FindMapElement(tiles(),Str(k)+"|"+Str(i-k))
      If *this <> #Null
        If i=0
          *this\RFactor = 1
          *this\BFactor = 1
          *this\GFactor = 1
          *this\RFactor2 = *this\RFactor
          *this\GFactor2 = *this\GFactor
          *this\BFactor2 = *this\BFactor
        Else
          If k = 0
            *neighbor.tile = FindMapElement(tiles(),Str(k)+"|"+Str(i-k-1))
            *this\RFactor = *neighbor\R_down_average / *this\R_up_average
            *this\GFactor = *neighbor\G_down_average / *this\G_up_average
            *this\BFactor = *neighbor\B_down_average / *this\B_up_average
            *this\RFactor2 = *this\RFactor
            *this\GFactor2 = *this\GFactor
            *this\BFactor2 = *this\BFactor
          ElseIf  k = i
            *neighbor.tile = FindMapElement(tiles(),Str(k-1)+"|"+Str(i-k))
            *this\RFactor = *neighbor\R_right_average / *this\R_left_average
            *this\GFactor = *neighbor\G_right_average / *this\G_left_average
            *this\BFactor = *neighbor\B_right_average / *this\B_left_average
            *this\RFactor2 = *this\RFactor
            *this\GFactor2 = *this\GFactor
            *this\BFactor2 = *this\BFactor
          Else
            *upper.tile = FindMapElement(tiles(),Str(k)+"|"+Str(i-k-1))
            *left.tile = FindMapElement(tiles(),Str(k-1)+"|"+Str(i-k))
;             Debug "Upper R_down: "+ *upper\R_down_average
;             Debug "This R_up: "+*this\R_up_average
;             Debug "This R_left: " + *this\R_left_average
;             Debug "Left R_right: "+ *left\R_right_average
;             End
            *neighbor.tile = FindMapElement(tiles(),Str(k)+"|"+Str(i-k-1))
            *this\RFactor = *upper\R_down_average / *this\R_up_average
            *this\GFactor = *upper\G_down_average / *this\G_up_average
            *this\BFactor = *upper\B_down_average / *this\B_up_average
            
            *this\RFactor2 = *left\R_right_average / *this\R_left_average
            *this\GFactor2 = *left\G_right_average / *this\G_left_average
            *this\BFactor2 = *left\B_right_average / *this\B_left_average
            
            ;*this\RFactor = 0.5*(*left\R_right_average / *this\R_left_average + *upper\R_down_average / *this\R_up_average);((*upper\R_down_average * *this\R_up_average) + (*left\R_right_average * *this\R_left_average)) / (*this\R_left_average * *this\R_left_average * *this\R_up_average * *this\R_up_average)
            ;*this\GFactor = 0.5*(*left\G_right_average / *this\G_left_average + *upper\G_down_average / *this\G_up_average);((*upper\G_down_average * *this\G_up_average) + (*left\G_right_average * *this\G_left_average)) / (*this\G_left_average * *this\G_left_average * *this\G_up_average * *this\G_up_average)
            ;*this\BFactor = 0.5*(*left\B_right_average / *this\B_left_average + *upper\B_down_average / *this\B_up_average);((*upper\B_down_average * *this\B_up_average) + (*left\B_right_average * *this\B_left_average)) / (*this\B_left_average * *this\B_left_average * *this\B_up_average * *this\B_up_average)
            
          EndIf
        EndIf
        applySingleFactor(ImageID,tiles(),k,i-k)
        setSingleBorderAverage(ImageID,tiles(),k,i-k)
      EndIf
    Next
  Next
  
        
EndProcedure

Structure HS
  H.d
  S.d
EndStructure


Global NewList lowColorPic.HS()

Procedure.d Max(r.d,g.d,b.d)
  max.d = r
  If g > max
    max = g
  EndIf
  If b > max
    max = b
  EndIf
  ProcedureReturn max
EndProcedure

Procedure.d Min(r.d,g.d,b.d)
  min.d = r
  If g < min
    min = g
  EndIf
  If b < min
    min = b
  EndIf
  ProcedureReturn min
EndProcedure

Procedure correctColors(ImageID.i, ImageIDLow.i)
  H.d = 0
  S.d = 0
  V.d = 0
  H2.d = 0
  S2.d = 0
  V2.d = 0
  R.d = 0
  G.d = 0
  B.d = 0
  Maxi.d = 0
  Mini.d = 0
  hi.d = 0
  p.d = 0
  q.d = 0
  t.d = 0
  f.d = 0
  ResizeImage(ImageIDLow, ImageWidth(ImageID), ImageHeight(ImageID))
  Debug ImageWidth(ImageIDLow)
  StartDrawing(ImageOutput(ImageIDLow))
  For y = 0 To ImageHeight(ImageIDLow)-1
    For x = 0 To ImageWidth(ImageIDLow)-1
    
      R = Red(Point(x,y)) / 255
      G = Green(Point(x,y)) / 255
      B = Blue(Point(x,y)) / 255
      AddElement(lowColorPic())
      Maxi = Max(R,G,B)
      Mini = Min(R,G,B)
      If Maxi = Mini
        H2 = 0
      ElseIf  Maxi = R
        H2 = 60 * ( (G-B) / (Maxi-Mini) )
      ElseIf Maxi = G
        H2 = 60 * (2+ (B-R) / (Maxi-Mini) )
      ElseIf Maxi = B
        H2 = 60 * (4+ (R-G) / (Maxi-Mini) )
      EndIf
      If H2 < 0
        H2 = H+360
      EndIf
      lowColorPic()\H = H2
      If Maxi = 0
        S = 0
      Else
        S = (Maxi-Mini)/Maxi
      EndIf
      lowColorPic()\S = S
    Next
  Next
  StopDrawing()
  Debug "ColorMap created!"
  StartDrawing(ImageOutput(ImageID))
  ResetList(lowColorPic())
  For y = 0 To ImageHeight(ImageID)-1
    For x = 0 To ImageWidth(ImageID)-1
      NextElement(lowColorPic())
      R = Red(Point(x,y)) / 255
      G = Green(Point(x,y)) / 255
      B = Blue(Point(x,y)) / 255
      Maxi = Max(R,G,B)
      Mini = Min(R,G,B)
;       If Maxi = Mini
;         H = 0
;       ElseIf  Maxi = R
;         H = 60 * ( (G-B) / (Maxi-Mini) )
;       ElseIf Maxi = G
;         H = 60 * (2+ (B-R) / (Maxi-Mini) )
;       ElseIf Maxi = B
;         H = 60 * (4+ (R-G) / (Maxi-Mini) )
;       EndIf
;       If H < 0
;         H = H+360
;       EndIf
      If Maxi = 0
        S = 0
      Else
        S = (Maxi-Mini)/Maxi
      EndIf
      V = Maxi
      
      ;SelectElement(lowColorPic(),y*ImageWidth(ImageID)+x)
      
      H = lowColorPic()\H
      If H > 360 Or H<0
        Debug "HEEE"
      EndIf
      
      S = lowColorPic()\S
      ;Debug S
      
      hi = Round(H/60,#PB_Round_Down)
      f=(H/60-hi)
      p = V*(1-S)
      q = V*(1-(S*f))
      t = V*(1-(S*(1-f)))
      If hi = 0 Or hi = 6
        Plot(x,y,RGB(V*255,t*255,p*255))
      ElseIf hi = 1
        Plot(x,y,RGB(q*255,V*255,p*255))
      ElseIf hi = 2
        Plot(x,y,RGB(p*255,V*255,t*255))
      ElseIf hi = 3
        Plot(x,y,RGB(p*255,q*255,V*255))
      ElseIf hi = 4
        Plot(x,y,RGB(t*255,p*255,V*255))
      ElseIf hi = 5
        Plot(x,y,RGB(V*255,p*255,q*255))
      EndIf
      
    Next
  Next
  StopDrawing()
EndProcedure

Procedure eliminateHardBorders(ImageID.i, Map tiles.tile(),size.i = 1)
  StartDrawing(ImageOutput(ImageID))
  
  ForEach tiles()
    startingPixelX = tiles()\x * tiles()\size
    startingPixelY = tiles()\y * tiles()\size
    
    SizeBorderSquare = tiles()\size * BorderSize 
    For x = startingPixelX+size To startingPixelX+1 Step -1
      For y = startingPixelY  To startingPixelY+tiles()\size-1
        Plot(x-1,y,Point(x,y))
      Next
    Next
    For x = startingPixelX+tiles()\size-1-size To startingPixelX+tiles()\size-2
      For y = startingPixelY  To startingPixelY+tiles()\size-1
        Plot(x+1,y,Point(x,y))
      Next
    Next
    For x = startingPixelX To startingPixelX+tiles()\size-1
      For y = startingPixelY+tiles()\size-1-size  To startingPixelY+tiles()\size-2
        Plot(x,y+1,Point(x,y))
      Next
    Next
    For x = startingPixelX To startingPixelX+tiles()\size-1
      For y = startingPixelY+size  To startingPixelY+1 Step -1
        Plot(x,y-1,Point(x,y))
      Next
    Next
  Next
  
  StopDrawing()
EndProcedure


;setFactors(tileMapLow(),tileMap())
;applyFactors(1,tileMap())

;setRelativeFactors(tileMap(),4,4)

;eliminateHardBorders(1,tileMap(),5)
;SaveImage(1,"NewImage_Borderless.jpg",#PB_ImagePlugin_JPEG,7)
setBroderAverages(1,tileMap())
;applyFactors(1,tileMap())
setBorderFactors(1,tileMap())
correctColors(1,0)
;overlayTiles(1,tileMap())

SaveImage(1,"NewImage.jpg",#PB_ImagePlugin_JPEG,7)

;FindMapElement(tileMap(),"8|1")
;Debug tileMap()\BAverage


; IDE Options = PureBasic 5.60 (Windows - x64)
; CursorPosition = 775
; FirstLine = 172
; Folding = AE9
; EnableXP