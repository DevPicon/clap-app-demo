//
//  AudioPlayer.swift
//  iosApp
//
//  Created by Armando Picon on 28-10-25.
//  Copyright © 2025 orgName. All rights reserved.
//


import AVFoundation

  class AudioPlayer {
      var player: AVAudioPlayer?

      init() {
          if let path = Bundle.main.path(forResource: "claps", ofType: "wav") {
              let url = URL(fileURLWithPath: path)
              player = try? AVAudioPlayer(contentsOf: url)
              player?.prepareToPlay()
          }
      }

      func play() {
          player?.currentTime = 0
          player?.play()
      }
  }
