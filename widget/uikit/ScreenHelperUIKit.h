/* -*- Mode: C++; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef mozilla_widget_uikit_ScreenHelperUIKit_h
#define mozilla_widget_uikit_ScreenHelperUIKit_h

#include "mozilla/widget/ScreenManager.h"

@class UIScreen;

namespace mozilla {
namespace widget {

class ScreenHelperUIKit final : public ScreenManager::Helper {
 public:
  ScreenHelperUIKit();

  void RefreshScreens();
};

}  // namespace widget
}  // namespace mozilla

#endif  // mozilla_widget_uikit_ScreenHelperUIKit_h
